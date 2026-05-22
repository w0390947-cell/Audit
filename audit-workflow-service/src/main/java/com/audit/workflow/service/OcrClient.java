package com.audit.workflow.service;

import java.awt.image.BufferedImage;

public interface OcrClient {

    String recognize(BufferedImage image, int pageNo);
}
