# OCR 服务优化方案

## 1. 背景

当前系统中，图片文件和扫描 PDF 的 OCR 能力由独立的 `ocr-service` 提供，主后端和工作流服务通过 HTTP 调用：

- 主后端审核文件库：`http://127.0.0.1:8866/ocr`
- 工作流服务临时依据解析：`http://127.0.0.1:8866/ocr`

近期测试发现，当一个审核任务包含多张图片依据文件时，工作流服务会逐个调用 OCR。由于图片尺寸较大、PaddleOCR 首次加载模型较慢，以及 OCR 服务缺少受控并发机制，出现过以下问题：

- OCR 请求超时。
- OCR 服务返回 `502 Bad Gateway`。
- 多个 OCR 请求可能同时访问同一个 PaddleOCR 全局实例，存在稳定性风险。
- OCR 服务异常日志不够详细，排查真实异常原因困难。

## 2. 当前处理链路

### 2.1 工作流服务

工作流服务在 `basis_file_parse` 节点解析用户上传的依据文件。

当前行为：

- 多个依据文件通过普通 `for` 循环逐个解析。
- 图片 `jpg/jpeg/png` 通过 `ImageDocumentParser` 调用 OCR 服务。
- 扫描 PDF 的空白页通过 `PdfDocumentParser` 渲染为图片后调用 OCR 服务。
- 单个任务内，多个图片文件或扫描 PDF 页面的 OCR 调用是串行的。

这意味着：一个任务中如果有 5 张图片，每张图片 OCR 耗时 2 分钟，单任务仅图片 OCR 就可能需要约 10 分钟。

### 2.2 OCR 服务

OCR 服务使用 FastAPI/uvicorn 暴露 `/ocr` 接口。

当前行为：

- PaddleOCR 使用全局变量 `_paddle_ocr` 懒加载。
- 接口收到请求后直接调用 `_paddle_ocr.predict(...)` 或 `_paddle_ocr.ocr(...)`。
- 没有显式锁、队列、并发池或请求限流。
- 当多个任务同时请求 OCR 时，可能并发访问同一个 PaddleOCR 实例。

这意味着：

- 单个工作流任务内部 OCR 基本是串行的。
- 多个任务同时运行时，OCR 服务可能并发处理多个请求，但并发是非受控的。
- 非受控并发可能带来模型初始化竞争、内存压力、PaddleOCR 线程安全风险和偶发 502。

## 3. 优化目标

优化目标按优先级分为两层。

### 3.1 稳定性目标

优先保证 OCR 服务稳定、可观测、可恢复：

- 避免多个请求同时初始化或调用同一个 PaddleOCR 实例。
- OCR 失败时记录完整异常堆栈。
- 返回明确的错误信息，方便主后端和工作流服务定位。
- 对大图片、慢 OCR 请求提供足够的等待时间。
- 避免一个异常请求影响后续请求。

### 3.2 吞吐量目标

在稳定性达标后，再逐步提升吞吐能力：

- 支持配置化的 OCR 并发度。
- 支持多 OCR worker 或多进程实例。
- 支持任务级排队、超时、降级和监控。
- 支持按部署机器能力调整并发策略。

## 4. 推荐实施路线

建议分三期实施。

## 5. 第一期：稳定性优先

第一期不追求并发加速，先解决 502 难排查和 PaddleOCR 非受控并发问题。

### 5.1 增加 OCR 服务异常日志

在 `/ocr` 接口异常捕获处增加 `logger.exception(...)`。

当前问题：

- OCR 服务返回 502 时，日志只看到 `POST /ocr HTTP/1.1 502 Bad Gateway`。
- 看不到 Python traceback。
- 无法判断是 PaddleOCR 内部异常、图片解码异常、内存异常，还是模型并发访问异常。

建议处理：

```python
except Exception as exc:
    logger.exception("ocr request failed, page_no=%s, provider=%s", payload.page_no, OCR_PROVIDER)
    raise HTTPException(status_code=502, detail=str(exc))
```

预期效果：

- 每次 502 都能看到完整异常堆栈。
- 能准确定位 PaddleOCR 报错原因。
- 后续优化可以基于真实异常，而不是猜测。

### 5.2 给 PaddleOCR 调用加锁

为 PaddleOCR 初始化和识别增加全局锁。

建议新增：

```python
import threading

_paddle_ocr = None
_paddle_lock = threading.Lock()
```

然后在 `run_paddle_ocr` 中包裹模型初始化和识别：

```python
with _paddle_lock:
    if _paddle_ocr is None:
        _paddle_ocr = create_paddle_ocr()

    if hasattr(_paddle_ocr, "predict"):
        result = _paddle_ocr.predict(tmp_path)
    else:
        result = _paddle_ocr.ocr(tmp_path, cls=True)
```

注意：

- 临时文件写入可以放在锁外。
- PaddleOCR 模型初始化和预测建议放在锁内。
- 这样会使单个 OCR 服务实例一次只处理一个 PaddleOCR 请求，但稳定性更高。

预期效果：

- 避免多个请求同时初始化 `_paddle_ocr`。
- 避免多个请求并发调用同一个 PaddleOCR 实例。
- 降低偶发 502 风险。

### 5.3 增加请求耗时日志

建议在 `/ocr` 接口记录以下信息：

- `page_no`
- `provider`
- 输入图片字节数
- OCR 耗时
- 识别文本长度
- 成功或失败状态

示例：

```python
logger.info(
    "ocr request completed, provider=%s, page_no=%s, bytes=%s, text_chars=%s, duration_ms=%s",
    OCR_PROVIDER,
    payload.page_no,
    len(image_bytes),
    len(text),
    duration_ms,
)
```

预期效果：

- 能判断大图 OCR 实际耗时。
- 能为超时配置提供依据。
- 能识别是否存在某类图片特别慢。

### 5.4 工作流 OCR 超时配置保持 600 秒

工作流服务已将 OCR 超时时间调整为 600 秒，与主后端审核文件库 OCR 配置对齐。

建议保留：

```yaml
audit:
  ocr:
    timeout-seconds: ${AUDIT_OCR_TIMEOUT_SECONDS:600}
```

说明：

- 大图片和扫描 PDF OCR 可能明显超过 60 秒。
- 600 秒适合作为本地 PaddleOCR 大图处理的保守默认值。
- 后续可根据耗时日志再调整。

## 6. 第二期：受控并发

第一期加锁后，OCR 服务会更稳定，但吞吐能力有限。第二期可以引入受控并发。

### 6.1 配置化 OCR 并发度

增加环境变量：

```env
OCR_MAX_CONCURRENCY=1
```

默认值建议为 `1`。

含义：

- `1`：稳定优先，单 OCR 实例串行处理。
- `2` 或更高：允许有限并发，但需要验证内存、CPU、PaddleOCR 稳定性。

实现方式可以使用 `threading.Semaphore`：

```python
_ocr_semaphore = threading.Semaphore(OCR_MAX_CONCURRENCY)

with _ocr_semaphore:
    text = run_paddle_ocr(image_bytes)
```

注意：

- 如果仍然使用单个 `_paddle_ocr` 实例，不建议简单放开并发。
- 若要真正并发，应考虑多个模型实例或多个服务进程。

### 6.2 增加队列等待日志

如果使用锁或信号量，建议记录排队等待时间：

- `queue_wait_ms`
- `ocr_duration_ms`
- `total_duration_ms`

这样可以区分：

- 请求慢是因为排队。
- 请求慢是因为 OCR 本身耗时。

### 6.3 超过队列等待上限时快速失败

可选增加：

```env
OCR_QUEUE_TIMEOUT_SECONDS=30
```

如果请求等待 OCR 执行槽位超过上限，可返回 `503 Service Unavailable`。

适用场景：

- 防止大量请求堆积。
- 给调用方明确反馈 OCR 服务繁忙。

当前本地测试阶段可以暂不启用快速失败，避免影响长耗时任务。

## 7. 第三期：多实例吞吐扩展

当业务量上升，仅靠单 OCR 实例串行处理会成为瓶颈。第三期可考虑多实例。

### 7.1 多端口 OCR 实例

在同一台机器启动多个 OCR 服务实例：

```text
127.0.0.1:8866
127.0.0.1:8867
127.0.0.1:8868
```

每个实例加载自己的 PaddleOCR 模型。

优点：

- 进程隔离，稳定性更好。
- 单实例崩溃不影响全部 OCR 能力。
- 可以按机器资源水平扩展实例数量。

缺点：

- 内存占用增加。
- 模型加载时间增加。
- 需要负载均衡或调用端轮询。

### 7.2 调用端负载均衡

可以在主后端和工作流服务中支持多个 OCR endpoint：

```yaml
audit:
  ocr:
    endpoints:
      - http://127.0.0.1:8866/ocr
      - http://127.0.0.1:8867/ocr
```

负载策略：

- 轮询。
- 随机。
- 最少失败优先。
- 健康检查后选择可用实例。

### 7.3 反向代理负载均衡

也可以在 OCR 服务前增加 Nginx 或其他本地代理：

```text
主后端/工作流服务 -> http://127.0.0.1:8866/ocr -> 多个 OCR worker
```

优点：

- 调用端无需感知多个实例。
- OCR 服务扩容对业务服务透明。

缺点：

- 部署复杂度增加。
- 本地开发环境需要额外配置代理。

## 8. 工作流层面的并发优化

OCR 服务稳定后，是否让工作流服务并发解析多个依据文件，需要谨慎评估。

### 8.1 当前不建议立即并发解析依据文件

原因：

- OCR 服务还没有受控并发能力。
- 大图 OCR 很耗 CPU 和内存。
- 并发解析可能让单任务速度更快，但会抢占其他任务资源。
- 扫描 PDF 多页并发 OCR 可能导致瞬时请求风暴。

### 8.2 后续可增加任务内 OCR 并发配置

可选配置：

```yaml
audit:
  ocr:
    file-parse-parallelism: 1
    pdf-page-parallelism: 1
```

默认保持 `1`。

当 OCR 服务支持多实例或受控并发后，再考虑调大。

### 8.3 推荐策略

短期：

- 工作流继续串行解析依据文件。
- OCR 服务加锁，保证稳定。
- 超时时间保持 600 秒。

中期：

- OCR 服务增加 `OCR_MAX_CONCURRENCY`。
- 工作流根据 OCR 服务能力小幅增加并发。

长期：

- OCR 服务多实例部署。
- 工作流支持可配置并发。
- 引入队列和任务级 OCR 进度监控。

## 9. 可观测性优化

建议 OCR 服务增加以下日志和接口。

### 9.1 健康检查

当前已有：

```http
GET /health
```

建议扩展返回：

```json
{
  "status": "ok",
  "provider": "paddle",
  "model_loaded": true,
  "max_concurrency": 1,
  "active_requests": 0,
  "queued_requests": 0
}
```

### 9.2 OCR 请求日志

建议每次请求至少记录：

- 请求开始时间。
- 请求结束时间。
- 图片字节数。
- 页面编号。
- OCR provider。
- OCR 耗时。
- 文本字符数。
- 是否成功。
- 失败异常堆栈。

### 9.3 慢请求日志

增加配置：

```env
OCR_SLOW_REQUEST_SECONDS=60
```

超过阈值时输出 warning：

```text
slow ocr request, duration_ms=125000, image_bytes=1163911, page_no=1
```

### 9.4 调用方日志

工作流服务和主后端可在 OCR 调用失败时记录：

- 文件名。
- 文件类型。
- 页码。
- OCR endpoint。
- 超时时间。
- 错误信息。

这样可以快速定位是哪一个图片或 PDF 页面导致失败。

## 10. 配置建议

### 10.1 OCR 服务

建议新增或保留以下环境变量：

```env
OCR_PROVIDER=paddle
OCR_MAX_CONCURRENCY=1
OCR_QUEUE_TIMEOUT_SECONDS=0
OCR_SLOW_REQUEST_SECONDS=60
```

说明：

- `OCR_MAX_CONCURRENCY=1`：默认稳定优先。
- `OCR_QUEUE_TIMEOUT_SECONDS=0`：表示不限制排队等待，适合本地测试和长任务。
- `OCR_SLOW_REQUEST_SECONDS=60`：超过 60 秒记录慢请求。

### 10.2 工作流服务

建议保持：

```yaml
audit:
  ocr:
    endpoint: ${AUDIT_OCR_ENDPOINT:http://127.0.0.1:8866/ocr}
    provider: ${AUDIT_OCR_PROVIDER:paddle}
    timeout-seconds: ${AUDIT_OCR_TIMEOUT_SECONDS:600}
```

### 10.3 主后端审核文件库

建议保持：

```yaml
vector:
  ocr:
    enabled: true
    endpoint: http://127.0.0.1:8866/ocr
    read-timeout: 600000
```

## 11. 测试方案

### 11.1 单图片 OCR 测试

准备一张清晰 PNG 图片，调用 `/ocr`：

- 预期返回 `200 OK`。
- 响应中 `text` 非空。
- 日志包含耗时、字节数、文本长度。

### 11.2 大图片 OCR 测试

使用尺寸接近 `3966x5613` 的图片：

- 预期不再因 60 秒超时失败。
- 若 OCR 服务内部异常，应有完整 traceback。
- 如果超过 60 秒，应输出慢请求日志。

### 11.3 多图片依据任务测试

创建包含多张 PNG 依据文件的审核任务：

- `basis_file_parse` 节点应等待每张图片 OCR 完成。
- 成功图片应出现在 `basis_files` 输出中，`parse_status=SUCCESS`。
- 后续 `basis_pack_or_match` 中的 `basis_file_count` 应包含图片解析成功后的数量。
- `audit_retrieval_reference` 中应能看到图片依据文件的引用。

### 11.4 并发任务测试

同时提交多个包含图片依据的审核任务：

- 第一期加锁后，OCR 请求应按顺序处理。
- 不应出现因 PaddleOCR 并发访问导致的 502。
- 任务总耗时会增加，但稳定性应提升。

### 11.5 扫描 PDF 测试

上传扫描 PDF：

- 空白文本页应触发 OCR。
- 每页 OCR 成功后应生成对应文本块。
- 大页数 PDF 的总耗时应在超时配置范围内。

## 12. 上线步骤

### 12.1 第一期上线

1. 修改 `ocr-service/server.py`：
   - 增加异常堆栈日志。
   - 增加 PaddleOCR 全局锁。
   - 增加请求耗时日志。
2. 确认工作流服务 OCR 超时为 600 秒。
3. 重启 OCR 服务。
4. 重启工作流服务。
5. 用包含多张 PNG 依据文件的任务复测。
6. 检查：
   - OCR 服务日志。
   - 工作流 `basis_file_parse` 节点输出。
   - 临时依据匹配节点输出。
   - 最终检索引用。

### 12.2 第二期上线

1. 增加 `OCR_MAX_CONCURRENCY` 配置。
2. 默认保持 `1`。
3. 在测试环境尝试 `2`。
4. 压测观察：
   - OCR 成功率。
   - 平均耗时。
   - 机器 CPU/内存。
   - 是否出现 502。

### 12.3 第三期上线

1. 部署多个 OCR 服务实例。
2. 增加负载均衡。
3. 配置健康检查。
4. 小流量验证。
5. 再逐步扩大并发。

## 13. 风险和注意事项

### 13.1 加锁后的耗时风险

加锁会让单实例 OCR 严格串行：

- 稳定性提升。
- 总耗时可能增加。
- 多任务同时运行时会排队。

这是第一期可接受的权衡，因为当前主要问题是 OCR 502 和不稳定。

### 13.2 多实例内存风险

多个 OCR 实例会各自加载模型：

- 内存占用会明显增加。
- 低内存机器可能变慢或崩溃。
- 扩容前需要观察实际内存占用。

### 13.3 工作流超时风险

工作流 OCR 超时提升到 600 秒后：

- 大图 OCR 更容易成功。
- 但单个节点可能运行更久。
- 如果大量图片都很慢，整体任务耗时会明显增加。

后续可以通过节点进度、OCR 队列状态和慢请求日志改善用户感知。

## 14. 推荐结论

当前建议先实施第一期：

- OCR 服务增加完整异常日志。
- PaddleOCR 初始化和调用加锁。
- 增加请求耗时和慢请求日志。
- 工作流服务 OCR 超时保持 600 秒。

这能最快解决当前测试中出现的 `request timed out` 和 `502 Bad Gateway` 难排查、不稳定问题。

等 OCR 服务稳定后，再进入第二期，通过 `OCR_MAX_CONCURRENCY` 和多实例部署逐步提升吞吐能力。
