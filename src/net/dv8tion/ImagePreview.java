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

import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;

/**
 * Provides functionality to view images before uploading them.
 * 
 * @author DV8FromTheWorld (Austin Keener)
 * @version v1.0.0  July 16, 2014
 *
 */
@SuppressWarnings("serial")
public class ImagePreview extends JFrame implements ActionListener
{
    private ImagePanel imagePanel;
    private ArrayList<File> images;
    private ArrayList<Image> imagesLoaded;

    private JMenuBar menuBar;
    private JButton previousButton;
    private JButton nextButton;
    private JLabel imageCount;

    private int imageIndex;
    
    /**
     * Creates a new instance of the ImagePreview GUI.
     * 
     * @param images
     *          The images that will be viewed.
     */
    public ImagePreview(ArrayList<File> images)
    {
        this.setTitle("Image Preview");
        this.setIconImage(UploaderFrame.IMAGE_ICON.getImage());

        this.imageIndex = 0;
        this.images = images;
        this.imagesLoaded = new ArrayList<Image>();

        imagePanel = new ImagePanel();
        menuBar = new JMenuBar();

        previousButton = new JButton("Previous");
        previousButton.setFont(UploaderFrame.FONT);
        previousButton.addActionListener(this);
        previousButton.setMargin(new Insets(0, 0, 0, 0));
        previousButton.setSize(20, 25);

        nextButton = new JButton("Next");
        nextButton.setFont(UploaderFrame.FONT);
        nextButton.addActionListener(this);
        nextButton.setMargin(new Insets(0, 0, 0, 0));
        nextButton.setSize(10, 25);

        imageCount = new JLabel("1 / " + images.size());
        imageCount.setFont(UploaderFrame.FONT);
        imageCount.setSize(5, 15);

        menuBar.add(previousButton);
        menuBar.add(nextButton);
        menuBar.add(imageCount);
        this.add(imagePanel);
        this.setJMenuBar(menuBar);

        loadImage();
        buttonStatusUpdate();
    }

    /**
     * Handles the button presses to switch between images.
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (previousButton == source)
        {
            imageIndex--;
            loadImage();
            buttonStatusUpdate();
            
        }
        else if (nextButton == source)
        {
            imageIndex++;
            loadImage();
            buttonStatusUpdate();
        }
    }

    /**
     * Loads an images based on the current image index.
     */
    private void loadImage()
    {
        Image i;
        if (imageIndex >= imagesLoaded.size())
        {
            File imageFile = images.get(imageIndex);
            if (imageIsBMP(imageFile))
            {
                try
                {
                    i = ImageIO.read(imageFile);
                }
                catch (IOException e)
                {
                    i = null;
                    e.printStackTrace();
                }
            }
            else
            {
                i = this.getToolkit().createImage(imageFile.getAbsolutePath());
            }
            imagesLoaded.add(i);
        }
        else
        {
            i = imagesLoaded.get(imageIndex);
        }
        imagePanel.setImage(i);        
        this.pack();
        repaint();
    }

    /**
     * Updates the previous and next buttons.  Also updates the image count.
     */
    private void buttonStatusUpdate()
    {
        previousButton.setEnabled(imageIndex != 0);
        nextButton.setEnabled(imageIndex < images.size() - 1);
        imageCount.setText((imageIndex + 1) + " / " + images.size());
    }

    /**
     * Helper method to determine if an image is BMP.
     * 
     * @param imageFile
     *          The image to check.
     * @return
     *          True if the image is a BMP.
     */
    private boolean imageIsBMP(File imageFile)
    {
        byte[] signature = new byte[2];
        FileInputStream fis;
        try
        {
            fis = new FileInputStream(imageFile);
            fis.read(signature);
            fis.close();
            return Arrays.equals(signature, UploaderFrame.BMP_SIGNATURE);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
