/**
 * Copyright 2014 DV8FromTheWorld (Austin Keener)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;

/**
 * This class provides the ability to screenshot a custom selected
 * part of the screen, and copy that screenshot to the System Clipboard.
 * 
 * @author DV8FromTheWorld (Austin Keener)
 * @version v1.0.3  July 17, 2014
 */
@SuppressWarnings("serial")
public class ScreenCapture extends JFrame
{
    private JPanel pnlCapture;
    private JButton btnCapture;

    /**
     * Creates a new instance of the Screen Capture GUI.
     */
    public ScreenCapture()
    {
        this.setTitle("Custom Capture");
        this.setSize(200, 200);
        this.setLocationRelativeTo(null);
        this.setIconImage(UploaderFrame.IMAGE_ICON.getImage());
        this.setUndecorated(true);
        this.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        this.setBackground(new Color(0, 0, 0, 0));

        pnlCapture = new JPanel();
        pnlCapture.setOpaque(false);

        btnCapture = new JButton("Copy to Clipboard");
        btnCapture.setMargin(new Insets(0, 0, 0, 0));
        btnCapture.setSize(30, 10);
        btnCapture.setFont(UploaderFrame.FONT);
        btnCapture.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (e.getSource() == btnCapture)
                {
                    captureArea();
                }
            }
        });
        pnlCapture.add(btnCapture);        
        this.add(pnlCapture);
    }

    /**
     * Captures the currently selected area of the screen and sends it to the 
     * System Clipboard as an image.
     * 
     * Sets the capture gui non-visible so it is not captured in the screenshot.
     */
    private void captureArea()
    {
        final Point loc = pnlCapture.getLocationOnScreen();
        this.setVisible(false);
        this.repaint();

        //We run the screen capture in a different thread so that we can wait while
        //the gui frame is becoming invisible. (We don't want the button in the image).
        new Thread(new Runnable() 
        {
            @Override
            public void run()
            {
                Robot r;
                Rectangle imageArea = new Rectangle(
                        loc.x, loc.y, pnlCapture.getWidth(), pnlCapture.getHeight());
                try
                {
                    Thread.sleep(200);
                    r = new Robot();            
                    BufferedImage i = r.createScreenCapture(imageArea);
                    UploaderFrame.CLIPBOARD.setContents(new ClipboardImage(i), null);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();   
                }
                catch (AWTException e)
                {
                    e.printStackTrace();
                }
                ScreenCapture.this.setVisible(true);
            }
        }).start();        
    }
}
