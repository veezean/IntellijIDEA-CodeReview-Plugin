package com.veezean.idea.plugin.codereviewer.common;

import io.netty.handler.codec.base64.Base64Decoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2021/6/15
 */
public class ImageIconHelper {

    private static final String ICON_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABdUlEQVQ4T52TQUsCYRCG33XVXHODOkkRSElBoIcQ21t4CLwkFN26R3+gS4foF9S1P6DXYL14E4MoTfIgBJUQCAkd8pCYputuzJghurtkA8Me9pv3e2bm/QTjWjmFA+sAtjFZpKHjXjBulFsAG5PV/p7Ok4BhVdxoCZAly99cZitwkfEhEuxwWsWYQDLn5YLVBQ3Fihvzcz1OtSDxd1TMlODx1QkS2t/8RK0uIluewmG8yQKjYSpA6Et+DV0NUFY6qL6LeKi6WNBW4CQ1g0S0zehri10UKy4+Hw50Uas7ITr6Ax0WMiWgfmd9Ou6e3QgHOlj2ayi9uLGrtP7WAs3gMi/hYKuJng6cqTKO9z5MVzpGQLfLko5Y6AuDYUaC1IKIWKjN2xmOMYGBeWjyqSsvfB4DRzsN3oCZsSyNRINUCx4moRwEERJJtuxBItqydyK1QFYe3j9RJHPTTENkRKBO8hKJ4KnWN1rp/C0t/Dzn+D9eZB46Mt+kt7lb5jbI3QAAAABJRU5ErkJggg==";

    public static ImageIcon getDefaultIcon() {
        try {
            Image image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(ICON_BASE64)));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ImageIcon();
    }
}
