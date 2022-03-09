package AltiSkinTool.misc;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ConstructSheet {

    public static void main(String[] args) throws IOException {
        String imgPath = "C:\\Users\\Aedifico\\Desktop\\alti_image_adventures\\resources\\dist\\loopyPNG\\00";

        BufferedImage temp = new BufferedImage(1024, 512, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = temp.createGraphics();
        int x = 0;
        int y = 0;
        for (int i = 0; i <= 41; i++) {
            if (x + 90 > 1024) {
                x = 0;
                y += 90;
            }

            BufferedImage img;
            if (i < 10)
                img = ImageIO.read(new File(imgPath + "0" + i + ".png"));
            else
                img = ImageIO.read(new File(imgPath + i + ".png"));

            g2d.drawImage(img, x, y, null);
            x += 90;
        }

        ImageIO.write(temp, "png",
                new File("C:\\Users\\Aedifico\\Desktop\\alti_image_adventures\\resources\\dist\\loopyPNG\\sheet.png"));
    }

}
