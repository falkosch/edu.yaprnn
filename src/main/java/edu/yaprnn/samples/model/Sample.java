package edu.yaprnn.samples.model;

import java.awt.Image;
import java.io.File;

public interface Sample {

  File getFile();

  String getName();

  String getLabel();

  float[] getTarget();

  String[] getLabels();

  float[] getInput();

  Image createPreview();

  String getMetaDescription();
}
