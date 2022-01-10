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
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Core class of the program.  Controls the central GUI and core logic.
 * 
 * @author DV8FromTheWorld (Austin Keener)
 * @version v1.0.2  July 17, 2014
 */
@SuppressWarnings("serial")
public class UploaderFrame extends JFrame implements ActionListener, WindowListener
{
    /**
     * Main entry point of the program.
     * 
     * @param args
     *          Command Line Arguments.
     */
    public static void main(String[] args)
    {
        UploaderFrame f = new UploaderFrame();
        f.setVisible(true);
    }

    public static final Clipboard CLIPBOARD =
            Toolkit.getDefaultToolkit().getSystemClipboard();
    public static final ImageIcon IMAGE_ICON =
            new ImageIcon(UploaderFrame.class.getResource("/assets/icon.png"), "Icon");
    public static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    public static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4e, 0x47};
    public static final byte[] JPG_SIGNATURE = {(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe0};
    public static final byte[] JPG_SIGNATURE2 = {(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe1};
    public static final byte[] GIF_SIGNATURE = {0x47, 0x49, 0x46, 0x38};
    public static final byte[] BMP_SIGNATURE = {0x42, 0x4d};

    public final int SIZE_GUI_X = 290;
    public final int SIZE_GUI_Y = 290;
    private final int BUTTON_HEIGHT = 20;
    private final Insets MARGIN = new Insets(0, 0, 0, 0);
    private final String UPLOAD_MESSAGE =
            "Upload and Preview Buttons are disabled\nuntil an image is in the Clipboard.";

    private JPanel panel;
    private TrayIcon trayIcon;

    private PopupMenu menu;
    private MenuItem menuShow;
    private MenuItem menuUpload;
    private MenuItem menuExit;

    private JButton btnUpload;
    private JButton btnPreview;
    private JButton btnCustomCapture;
    private JButton btnOpenBrowser;
    private JButton btnCopyLink;

    private JTextArea lblLink;
    private JLabel lblTitle;
    private JTextPane lblUploadMessage;

    private ArrayList<File> imagesToUpload;
    private ArrayList<String> imageIds;
    private String url;
    private boolean uploading;

    /**
     * Creates a new UploaderFrame GUI, completely setup.
     * Does not call .setVisible(true), this will need to be done after creation.
     */
    public UploaderFrame()
    {
        initVisualComponents();
        imagesToUpload = new ArrayList<File>();
        imageIds = new ArrayList<String>();
        uploading = false;
    }

    /**
     * Sets the look and feel of the Program's GUI.
     *
     * @param system
     *          If true, will use the Look and Feel of the OS.
     */
    public static void setLookAndFeel(boolean system)
    {
        try
        {
            UIManager.setLookAndFeel(
                    system ? UIManager.getSystemLookAndFeelClassName()
                            : UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Controls what happens when any of the buttons are pressed.
     * 
     * @param e
     *          The event of the button click.
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (source == btnUpload || source == menuUpload)
        {
            showProgram();
            upload();
        }
        else if (source == btnPreview)
        {
            loadImages();
            new ImagePreview(imagesToUpload).setVisible(true);
        }
        else if (source == btnCustomCapture)
        {
            UploaderFrame.setLookAndFeel(false);
            new ScreenCapture().setVisible(true);
            UploaderFrame.setLookAndFeel(true);
        }
        else if (source == btnOpenBrowser)
        {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
            {
                try
                {
                    desktop.browse(new URI(url));
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        else if (source == btnCopyLink)
        {
            Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(url), null);
            uploadButtonStatus();
        }
        else if (source == menuShow)
        {
            showProgram();
        }
        else if (source == menuExit)
        {
            handleClosing();
        }
    }

    /**
     * Returns the size of the image list.
     * @return
     *          The amount of images files in the list to upload.
     */
    public int getImageAmount()
    {
        return imagesToUpload.size();
    }

    /**
     * Gets a specific image file from an index.
     * 
     * @param index
     *          The index of the image file in the image list.
     * @return
     *          The file of the image referenced by the index.
     */
    public File getImage(int index)
    {
        if (index >= 0 && index < imagesToUpload.size())
        {
            return imagesToUpload.get(index);
        }
        return null;
    }

    /**
     * Clears out all of the image file entries in the image list.
     */
    public void clearImages()
    {
        imagesToUpload.clear();
    }

    /**
     * Initializes all visual components of the GUI and adds them to the GUI.
     * Also sets the GUI's settings.
     */
    private void initVisualComponents()
    {
        setLookAndFeel(true);
        this.setTitle("Imgur Uploader");
        this.setSize(SIZE_GUI_X, SIZE_GUI_Y);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setResizable(false);
        this.addWindowListener(this);

        panel = new JPanel();
        panel.setLayout(null);

        btnUpload = new JButton();
        btnUpload.setText("Upload");
        btnUpload.setFont(FONT);
        btnUpload.setMargin(MARGIN);
        btnUpload.setLocation(93, 39);
        btnUpload.setSize(104, 38);
        btnUpload.addActionListener(this);
        
        btnPreview = new JButton();
        btnPreview.setText("Preview");
        btnPreview.setFont(FONT);
        btnPreview.setMargin(MARGIN);
        btnPreview.setLocation(110, 15);
        btnPreview.setSize(70, BUTTON_HEIGHT);
        btnPreview.addActionListener(this);
        
        btnCustomCapture = new JButton();
        btnCustomCapture.setText("Custom Screen Capture");
        btnCustomCapture.setFont(FONT);
        btnCustomCapture.setMargin(MARGIN);
        btnCustomCapture.setLocation(74, 130);
        btnCustomCapture.setSize(140, BUTTON_HEIGHT);
        btnCustomCapture.addActionListener(this);
        
        btnOpenBrowser = new JButton();
        btnOpenBrowser.setText("Open in Browser");
        btnOpenBrowser.setFont(FONT);
        btnOpenBrowser.setMargin(MARGIN);
        btnOpenBrowser.setLocation(15, 208);
        btnOpenBrowser.setSize(100, BUTTON_HEIGHT);
        btnOpenBrowser.addActionListener(this);
        btnOpenBrowser.setEnabled(false);
        
        btnCopyLink = new JButton();
        btnCopyLink.setText("Copy Link");
        btnCopyLink.setFont(FONT);
        btnCopyLink.setMargin(MARGIN);
        btnCopyLink.setLocation(152, 208);
        btnCopyLink.setSize(100, BUTTON_HEIGHT);
        btnCopyLink.addActionListener(this);
        btnCopyLink.setEnabled(false);
        
        lblLink = new JTextArea();
        lblLink.setText("NO CURRENT LINK");
        lblLink.setFont(FONT);
        lblLink.setLocation(88, 160);
        lblLink.setSize(190, 40);
        lblLink.setBackground(null);
        lblLink.setEditable(false);
        lblLink.setWrapStyleWord(true);
        lblLink.setLineWrap(true);
        
        lblTitle = new JLabel();
        lblTitle.setText("Imgur Link:");
        lblTitle.setFont(FONT);
        lblTitle.setLocation(15, 157);
        lblTitle.setSize(65, 20);
        
        lblUploadMessage = new JTextPane();
        lblUploadMessage.setText(UPLOAD_MESSAGE);
        lblUploadMessage.setLocation(25, 80);
        lblUploadMessage.setSize(235, 35);
        lblUploadMessage.setBackground(null);
        lblUploadMessage.setEditable(false);
        setupTextCentering(lblUploadMessage);

        panel.add(btnUpload);
        panel.add(btnPreview);
        panel.add(btnCustomCapture);
        panel.add(btnOpenBrowser);
        panel.add(btnCopyLink);
        panel.add(lblLink);
        panel.add(lblTitle);
        panel.add(lblUploadMessage);
        this.add(panel);
        this.setIconImage(IMAGE_ICON.getImage());

        if (!SystemTray.isSupported())
        {
            return;
        }

        menuShow = new MenuItem("Show");
        menuShow.addActionListener(this);
        menuShow.setFont(FONT);

        menuUpload = new MenuItem("Upload");
        menuUpload.addActionListener(this);
        menuUpload.setFont(FONT);

        menuExit = new MenuItem("Exit");
        menuExit.addActionListener(this);
        menuExit.setFont(FONT);

        menu = new PopupMenu();
        menu.add(menuShow);
        menu.add(menuUpload);
        menu.add(menuExit);

        trayIcon = new TrayIcon(IMAGE_ICON.getImage(), "Imgur Uploader", menu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent evt)
            {
                if (evt.getModifiers() == MouseEvent.BUTTON1_MASK)
                {
                    showProgram();
                }
            }
        });
        try
        {
            SystemTray.getSystemTray().add(trayIcon);
        }
        catch (AWTException e)
        {
            e.printStackTrace();
        }
    }

    private void upload()
    {
        uploading = true;
        loadImages();
        if (imagesToUpload.size() > 1)
        {
            lblLink.setText("Uploading " + imagesToUpload.size() + " images...\n"
                + "Completed: 0%");
            getAlbumWorker().execute();
        }
        else
        {
            lblLink.setText("Uploading and fetching URL...");
            getUploadWorker().execute();
        }
        btnUpload.setEnabled(false);
        btnPreview.setEnabled(false);
        btnOpenBrowser.setEnabled(false);
        btnCopyLink.setEnabled(false);
        if (menuUpload != null)
        {
            menuUpload.setEnabled(false);
        }
    }
    
    /**
     * Checks to see that there are images in the clipboard to upload.
     * 
     * @return
     * 			True if there is 1 or more images in the clipboard.
     */
    private boolean clipboardContainsImage()
    {
        loadImages();
        int count = imagesToUpload.size();
        clearImages();
        return count > 0;
    }

    /**
     * Loads files and checks to see that they are images.
     */
    @SuppressWarnings("unchecked")
    private void loadImages()
    {
        clearImages();
        if (CLIPBOARD.isDataFlavorAvailable(DataFlavor.imageFlavor))
        {
            try
            {
                BufferedImage image = (BufferedImage) CLIPBOARD.getData(DataFlavor.imageFlavor);
                File imageFile = new File("ClipboardImage.png");
                ImageIO.write(image, "png", imageFile);
                imagesToUpload.add(imageFile);
            }
            catch (UnsupportedFlavorException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return;
        }
        if (CLIPBOARD.isDataFlavorAvailable(DataFlavor.javaFileListFlavor))
        {
            try
            {
                for (File f : (List<File>) CLIPBOARD.getData(DataFlavor.javaFileListFlavor))
                {
                    if (f.isDirectory())
                    {
                        continue;
                    }

                    byte[] signature = new byte[4];
                    FileInputStream fis = new FileInputStream(f);
                    fis.read(signature);
                    if (Arrays.equals(signature, PNG_SIGNATURE)
                            || Arrays.equals(signature, JPG_SIGNATURE)
                            || Arrays.equals(signature, JPG_SIGNATURE2)
                            || Arrays.equals(signature, GIF_SIGNATURE)
                            || Arrays.equals(Arrays.copyOf(signature, 2), BMP_SIGNATURE))
                    {
                        imagesToUpload.add(f);
                    }
                    fis.close();
                }
            }
            catch (UnsupportedFlavorException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return;
        }
    }

    /**
     * Updates the Upload and Preview button statuses.
     */
    private void uploadButtonStatus()
    {
        if (!uploading)
        {
            if (clipboardContainsImage())
            {
                btnUpload.setEnabled(true);
                btnPreview.setEnabled(true);
                lblUploadMessage.setText("");
                if (menuUpload != null)
                {
                    menuUpload.setEnabled(true);
                }
            }
            else
            {
                btnUpload.setEnabled(false);
                btnPreview.setEnabled(false);
                lblUploadMessage.setText(UPLOAD_MESSAGE);
                if (menuUpload != null)
                {
                    menuUpload.setEnabled(false);
                }
            }
        }
    }

    /**
     * Displays the link generated from the upload.
     * Enables the OpenBrowser and CopyLink buttons.
     */
    private void uploadComplete()
    {
        uploading = false;
        lblLink.setText(url);
        uploadButtonStatus();
        clearImages();
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
        {
            try
            {
                new URI(url);
                btnOpenBrowser.setEnabled(true);
                btnCopyLink.setEnabled(true);
            }
            catch (URISyntaxException e)
            {
                btnOpenBrowser.setEnabled(false);
                btnCopyLink.setEnabled(false);
            }
        }
    }

    /**
     * Creates a new instance of a Swing Worker for Image uploading.
     * 
     * @return
     *          A new instance of the ImageUpload Swing worker.
     */
    private SwingWorker<String, Void> getUploadWorker()
    {
        return new SwingWorker<String, Void>()
                {

                    @Override
                    protected String doInBackground() throws Exception
                    {
                        File imageFile = imagesToUpload.get(0);
                        for (int attempt = 1; attempt <= Uploader.MAX_UPLOAD_ATTEMPTS; attempt++)
                        {
                            try
                            {
                                return getLink(Uploader.upload(imagesToUpload.get(0)));
                            }
                            catch (WebException e)
                            {
                                if (attempt >= Uploader.MAX_UPLOAD_ATTEMPTS)
                                {
                                    switch (showUploadError(e, imageFile, false))
                                    {
                                        case JOptionPane.YES_OPTION:
                                            attempt = 1;
                                            break;
                                    }
                                }
                            }
                        }
                        return "Upload was canceled due to error";
                    }
                    
                    @Override
                    protected void done()
                    {
                        try
                        {
                            url = get();
                            uploadComplete();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        catch (ExecutionException e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
    }

    /**
     * Creates a new instance of a Swing Worker for Album uploading.
     * 
     * @return
     *          A new instance of the Album Swing Worker.
     */
    private SwingWorker<String, Integer> getAlbumWorker()
    {
        return new SwingWorker<String, Integer>()
                {
                    @Override
                    protected String doInBackground() throws Exception
                    {
                        boolean uploadCanceled = false;
                        File imageFile;
                        for (int i = 0; !uploadCanceled && i < imagesToUpload.size(); i++)
                        {
                            this.publish((int)(((double) i) / imagesToUpload.size() * 100));
                            imageFile = imagesToUpload.get(i);
                            for (int attempt = 1; attempt <= Uploader.MAX_UPLOAD_ATTEMPTS; attempt++)
                            {
                                try
                                {
                                    imageIds.add(getId(Uploader.upload(imageFile)));
                                    break;
                                }
                                catch (WebException e)
                                {
                                    if (attempt >= Uploader.MAX_UPLOAD_ATTEMPTS)
                                    {
                                        switch (showUploadError(e, imageFile, true))
                                        {
                                            case JOptionPane.YES_OPTION:
                                                attempt = 1;
                                                break;
                                            case JOptionPane.CANCEL_OPTION:
                                                uploadCanceled = true;
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                        if (imageIds.size() == 0)
                        {
                            return "No images uploaded succesfully";
                        }
                        if (uploadCanceled)
                        {
                            return "Album upload canceled due to error";
                        }
                        for (int attempt = 1; attempt <= Uploader.MAX_UPLOAD_ATTEMPTS; attempt++)
                        {
                            try
                            {
                                return "https://imgur.com/a/" + getId(Uploader.createAlbum(imageIds));
                            }
                            catch (WebException e)
                            {
                                if (attempt >= Uploader.MAX_UPLOAD_ATTEMPTS)
                                {
                                    int result = JOptionPane.showConfirmDialog(null,
                                                "Images uploaded successfully, but could not be put into an album.\n" +
                                                "This was caused by the following error:\n" +
                                                        "              [" + e.getMessage() + "]\n" +
                                                "Would you like to attempt to put them into an album again?",
                                                "Album Creation Error", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                                    if (result == JOptionPane.YES_OPTION)
                                    {
                                        attempt = 0;
                                    }
                                }
                            }
                        }
                        return "Album creation canceled due to error";
                    }
                    
                    @Override
                    protected void done()
                    {
                        try
                        {
                            url = get();
                            imageIds.clear();
                            uploadComplete();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        catch (ExecutionException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void process(List<Integer> chunks)
                    {
                        lblLink.setText(
                                String.format("Uploading %d images...\nCompleted: %d%%",
                                        imagesToUpload.size(), chunks.get(0)));
                    }
                };
    }

    /**
     * Helper method that sets a JTextPane to have text centering.
     */
    private void setupTextCentering(JTextPane pane)
    {
        StyledDocument doc = pane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
    }

    /**
     * Uses Regex on the provided JSON String to find the 'link' tag.
     * 
     * @param jsonResponse
     *          The JSON response from Imgur.
     * @return
     *          The link to the image.
     */
    private String getLink(String jsonResponse)
    {
        Pattern pattern = Pattern.compile("link\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(jsonResponse);
        matcher.find();
        return matcher.group().replace("link\":\"", "").replace("\"", "").replace("\\/", "/");
    }

    /**
     * Uses Regex on the provided JSON String to find the 'id' tag.
     * 
     * @param jsonResponse
     *          The JSON response from Imgur.
     * @return
     *          The id of the image or album.
     */
    private String getId(String jsonResponse)
    {
        Pattern pattern = Pattern.compile("id\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(jsonResponse);
        matcher.find();
        return matcher.group().replace("id\":\"", "").replace("\"", "");
    }

    /**
     * Shows the program in the TaskBar and unminimizes it.
     */
    private void showProgram()
    {
        this.setExtendedState(Frame.NORMAL);
        this.setVisible(true);
        this.toFront();
    }

    /**
     * Method that handles closing.  Asks user if they are sure they want to close.
     */
    private void handleClosing()
    {
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to close Imgur Uploader?", "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, IMAGE_ICON);
        if (JOptionPane.YES_OPTION == result)
        {
            this.dispose();
            System.exit(0);
        }
    }

    /**
     * Displays a message box informing the user that an image failed to upload.
     * The user can choose to try uploading the image again, or to skip it.
     *
     * If the album boolean field is true, then the user will be presented with the
     * option to cancel the entire album upload.
     *
     * @param error
     *          The WebException that occurred.
     * @param imageFile
     *          The image file that failed to upload.
     * @param album
     *          True if the image that is being upload is part of an album upload.
     * @return
     *          JOptionPane.YES_OPTION - specifies that uploading should be attempted again.
     *          JOptionPane.NO_OPTION  - specifies that the image should be skipped.
     *          JOptionPane.CANCEL_OPTION - specifies that the entire album upload should be canceled.
     */
    private int showUploadError(WebException error, File imageFile, boolean album)
    {
        int result = JOptionPane.showConfirmDialog(null,
                "Uploader encountered the following problem:\n" +
                        "              ["+ error.getMessage() + "]\n" +
                "while attempting to upload:\n" +
                        "         " + imageFile.getAbsolutePath() + " \n" +
                "Would you like to try uploading the image again?", "Uploader Error",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION)
        {
            return JOptionPane.YES_OPTION;
        }
        if (!album)
        {
            return JOptionPane.NO_OPTION;
        }
        result = JOptionPane.showConfirmDialog(null,
                "Would you like to cancel the entire upload?", "Uploader Error",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION ? JOptionPane.CANCEL_OPTION : result;
    }

    /**
     * Called when the GUI gains focus.
     * 
     * @param ev
     *          The WindowEvent associated with the window gaining focus.
     */
    @Override
    public void windowActivated(WindowEvent ev)
    {
        uploadButtonStatus();
    }

    /**
     * Called when the GUI loses focus.
     * 
     * @param ev
     *          The WindowEvent associated with the window losing focus.
     */
    @Override
    public void windowDeactivated(WindowEvent ev)
    {

    }

    /**
     * Called when the GUI has closed.
     * 
     * @param ev
     *          The WindowEvent associated with the window closing completely.
     */
    @Override
    public void windowClosed(WindowEvent ev)
    {

    }

    /**
     * Called when the GUI is told to close (alt-f4, [x] button).
     * 
     * @param ev
     *          The WindowEvent associated with the window closing.
     */
    @Override
    public void windowClosing(WindowEvent ev)
    {
        handleClosing();
    }


    /**
     * Called when the GUI is unminimized.
     * 
     * @param ev
     *          The WindowEvent associated with the window unminimizing.
     */
    @Override
    public void windowDeiconified(WindowEvent ev)
    {
        uploadButtonStatus();
    }

    /**
     * Called when the GUI is minimized.
     * 
     * @param ev
     *          The WindowEvent associated with the window minimizing.
     */
    @Override
    public void windowIconified(WindowEvent ev)
    {
        Point mp = MouseInfo.getPointerInfo().getLocation();
        Point loc = this.getLocation();
        mp.x -= loc.x;
        mp.y -= loc.y;
        if (contains(mp) && trayIcon != null)
        {
            this.setVisible(false);
        }
    }

    /**
     * Called when the GUI is first opened.
     * 
     * @param ev
     *          The WindowEvent associated with the window opening.
     */
    @Override
    public void windowOpened(WindowEvent ev)
    {

    }
}
