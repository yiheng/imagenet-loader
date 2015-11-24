package com.intel.image_loader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import static com.intel.image_loader.Utils.*;

enum ImageType {
    JPEG
}

/**
 * Image file
 */
public class Image {
    public final String fileName;
    public final Path path;
    public final String label;
    public final String key;
    public final ImageType type;

    public static int widthScale = 256;
    public static int heightScale = 256;
    public static int cropWidth = 224;
    public static int cropHeight = 224;
    public static int nChannels = 3;

    public static double[] getBuffer() {
        return new double[Image.cropHeight * Image.cropWidth * Image.nChannels];
    }

    public static void extract(double[] data, String path) throws IOException {
        BufferedImage bufferImage =new BufferedImage(Image.cropWidth, Image.cropHeight, BufferedImage.TYPE_3BYTE_BGR);
        final int frameLength = Image.cropWidth * Image.cropHeight;
        for(int x = 0; x < Image.cropWidth; x++) {
            for(int y = 0; y < Image.cropHeight; y++) {
                int r = (int)(data[x + y * Image.cropWidth] * 255);
                int g = (int)(data[x + y * Image.cropWidth + frameLength] * 255);
                int b = (int)(data[x + y * Image.cropWidth + frameLength * 2] * 255);
                int rgb = r << 16 | g << 8 | b | 0xff000000;
                bufferImage.setRGB(x, y, rgb);
            }
        }
        File outputfile = new File(path);
        ImageIO.write(bufferImage, "jpg", outputfile);
    }

    public Image(Path path) {
        this.path = path;
        this.fileName = this.path.getFileName().toString();
        //String[] tokens = this.fileName.split("_");
        //requires(tokens.length == 2);
        this.label = this.path.getParent().getFileName().toString();
        //requires(this.label.equals(this.path.getParent().getFileName().toString()));
        String[] nameAndType = this.fileName.split("\\.");
        requires(nameAndType.length == 2);
        if (nameAndType[1].equals("JPEG")) {
            type = ImageType.JPEG;
        } else {
            throw new RuntimeException("Unsupported image type " + nameAndType[1]);
        }
        this.key = this.label + "_" + nameAndType[0];
    }

    public boolean load(double[] data) {
        try {
            // Read original image from file
            FileInputStream fis = new FileInputStream(path.toString());
            FileChannel channel = fis.getChannel();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            channel.transferTo(0, channel.size(), Channels.newChannel(byteArrayOutputStream));
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

            // Scale image
            int widthAfterScale = widthScale;
            int heightAfterScale = heightScale;
            if(img.getWidth()  < img.getHeight() ) {
                heightAfterScale = widthScale * img.getHeight() / img.getWidth();
            } else {
                widthAfterScale = heightScale * img.getWidth() / img.getHeight();
            }
            java.awt.Image scaledImage = img.getScaledInstance(
                    widthAfterScale, heightAfterScale, java.awt.Image.SCALE_SMOOTH);
            BufferedImage imageBuff = new BufferedImage(widthAfterScale, heightAfterScale, BufferedImage.TYPE_3BYTE_BGR);
            imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0, 0, 0), null);

            // Crop image
            int startX = (widthAfterScale - cropWidth) / 2;
            int startY = (heightAfterScale - cropHeight) / 2;
            convertToArray(imageBuff, startX, startY, cropWidth, cropHeight, data);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Can't read file " + fileName);
            return false;
        }
    }

    private void convertToArray(BufferedImage image, int startX, int startY, int w, int h, double[] result) {
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        if(image.getAlphaRaster() != null) {
            throw new UnsupportedOperationException("Not support img with alpha channel");
        }

        requires(pixels.length % nChannels == 0);
        final int frameLength = w * h ;
        requires(result.length == frameLength * nChannels);
        final double RGB = 255.0;
        final int originW = image.getWidth();
        final int originH = image.getHeight();
        final int offset = originW * startY + startX;
        for (int i = 0; i < frameLength; i++) {
            result[i] = (pixels[(offset + (i / w) * originW + (i % w)) * 3 + 2] & 0xff) / RGB;
            result[i + frameLength] = (pixels[(offset + (i / w) * originW + (i % w)) * 3 + 1] & 0xff) / RGB;
            result[i + frameLength * 2] = (pixels[(offset + (i / w) * originW + (i % w)) * 3] & 0xff) / RGB;
        }
    }
}
