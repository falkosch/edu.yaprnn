package yaprnn.dvv;

import java.util.Collection;
import java.util.Objects;
import yaprnn.mlp.TangensHyperbolicus;

class TestDVV {

  static final TangensHyperbolicus tanh = new TangensHyperbolicus();

  public static void main(String[] args) {
    TestDVV.test01();
    TestDVV.test02();
    TestDVV.test03();
    TestDVV.test04();
    TestDVV.test05();
    TestDVV.test06();
  }

  public static void test01() {
    byte[][] img = new byte[1][1];
    img[0][0] = 100;
    IdxPicture pic = new IdxPicture(img, "0", "file", 0);
    try {
      pic.subsample(1, 0.95, tanh);
      if (pic.getData()[0] != tanh.compute(100.0)) {
        System.out.println("Error in DVV-Test 01");
      }
      pic.subsample(1, 0.0, tanh);
      if (pic.getData()[0] != tanh.compute(100.0)) {
        System.out.println("Error in DVV-Test 01");
      }
      pic.subsample(0, 0.0, tanh);
      pic.subsample(2, 0.0, tanh);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static void test02() {
    byte[][] img = new byte[100][100];
    for (int i = 0; i < img.length; i++) {
      for (int j = 0; j < img.length; j++) {
        img[i][j] = 50;
      }
    }
    IdxPicture pic = new IdxPicture(img, "0", "file", 0);
    for (int k = 0; k <= 95; k++) {
      byte[][] data = (byte[][]) pic.previewSubsampledData(23, k / 100.0);
      for (byte[] datum : data) {
        for (int j = 0; j < data.length; j++) {
          if (datum[j] != 50) {
            System.out.println("Error in DVV-Test 02");
          }
        }
      }
    }
  }

  public static void test03() {
    byte[][] img = new byte[10][10];
    for (int i = 0; i < img.length; i++) {
      for (int j = 0; j < img.length; j++) {
        img[i][j] = 0;
      }
    }
    for (int i = 0; i < img.length; i++) {
      img[i][i] = 10;
    }
    IdxPicture pic = new IdxPicture(img, "0", "file", 0);
    byte[][] data = (byte[][]) pic.previewSubsampledData(5, 0.0);
    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data.length; j++) {
        if (i == j && data[i][j] != 5) {
          System.out.println("Error in DVV-Test 03");
        }
        if (i != j && data[i][j] != 0) {
          System.out.println("Error in DVV-Test 03");
        }
      }
    }
  }

  public static void test04() {
    byte[][] img = {{1, -1, 1, -1}, {1, -1, 1, -1}, {1, -1, 1, -1}, {1, -1, 1, -1}};
    IdxPicture pic = new IdxPicture(img, "0", "file", 0);
    byte[][] data = (byte[][]) pic.previewSubsampledData(2, 0.0);
    int r = 128;
    for (byte[] datum : data) {
      for (int j = 0; j < data.length; j++) {
        if (datum[j] != (byte) r) {
          System.out.println("Error in DVV-Test 04");
        }
      }
    }
  }

  public static void test05() {
    byte[][] img = {{-128, 127}, {127, -128}};
    IdxPicture pic = new IdxPicture(img, "0", "file", 0);
    byte[][] data = (byte[][]) pic.previewSubsampledData(1, 0.95);
    int r = 510 / 4;
    for (byte[] datum : data) {
      for (int j = 0; j < data.length; j++) {
        if (datum[j] != (byte) r) {
          System.out.println("Error in DVV-Test 05");
        }
      }
    }
  }

  public static void test06() {
    DVV dvv = null;
    try {
      dvv = new DVV("images", "labels");
    } catch (Exception e) {
      e.printStackTrace();
    }
    Objects.requireNonNull(dvv).chooseRandomTrainingData(0.3, 0.1);
    try {
      dvv.preprocess(10, 0.5, tanh);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    Collection<Data> training = dvv.getTrainingData();
    Collection<Data> test = dvv.getTestData();
    System.out.println("Training data count: " + training.size());
    System.out.println("Test data count: " + test.size());
    for (Data e : training) {
      for (Data d : test) {
        if (e.equals(d)) {
          System.out.println("Error in test06");
        }
      }
    }
  }

}
