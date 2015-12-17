package com.intel.image_loader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.nio.ByteBuffer;
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
    public static int nChannels = 3;
    private int width;
    private int height;

    public static void extract(byte[] data, String path) throws IOException {
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        int width = dataBuffer.getInt();
        int height = dataBuffer.getInt();
        int offset = 8;
        BufferedImage bufferImage =new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int r = data[(x + y * width) * 3 + 2 + offset] & 0xff;
                int g = data[(x + y * width) * 3 + 1 + offset] & 0xff;
                int b = data[(x + y * width) * 3 + offset] & 0xff;
                int rgb = r << 16 | g << 8 | b | 0xff000000;
                bufferImage.setRGB(x, y, rgb);
            }
        }
        File outputFile = new File(path);
        ImageIO.write(bufferImage, "jpg", outputFile);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Image(Path path) {
        this.path = path;
        this.fileName = this.path.getFileName().toString();
        this.label = this.path.getParent().getFileName().toString();
        String[] nameAndType = this.fileName.split("\\.");
        requires(nameAndType.length == 2);
        if (nameAndType[1].equals("JPEG")) {
            type = ImageType.JPEG;
        } else {
            throw new RuntimeException("Unsupported image type " + nameAndType[1]);
        }
        this.key = this.label + "_" + nameAndType[0];
    }

    public byte[] load() {
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
            width = imageBuff.getWidth();
            height = imageBuff.getHeight();
            return convertToArray(imageBuff);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Can't read file " + fileName);
            throw new RuntimeException(ex);
        }
    }

    private byte[] convertToArray(BufferedImage image) {
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        if(image.getAlphaRaster() != null) {
            throw new UnsupportedOperationException("Not support img with alpha channel");
        }
        requires(pixels.length % nChannels == 0);
        return pixels;
    }
}
