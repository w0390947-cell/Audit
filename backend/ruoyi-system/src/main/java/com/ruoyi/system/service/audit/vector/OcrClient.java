package com.ruoyi.system.service.audit.vector;

import java.awt.image.BufferedImage;

public interface OcrClient
{
    String recognize(BufferedImage image, int pageNo);
}
