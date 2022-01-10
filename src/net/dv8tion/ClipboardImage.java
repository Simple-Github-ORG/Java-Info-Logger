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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * Transferable data object that handles images for the System Clipboard.
 * 
 * @author DV8FromTheWorld (Austin Keener)
 * @version v1.0.0  July 16, 2014
 */
public class ClipboardImage implements Transferable
{
    private Image image;

    /**
     * Creates a new instance loaded with the provided image.
     * 
     * @param image
     *          The image to handle.
     */
    public ClipboardImage(Image image)
    {
        this.image = image;
    }

    /**
     * Gets the image stored in this Transferable object.
     * 
     * @param flavor
     *          The DataFlavor type (DataFlabor.imageFlavor).
     * @throws UnsupportedFlavorException
     *          Thrown if the requested data flavor is not supported.
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
        if (isDataFlavorSupported(flavor))
        {
            return image;
        }
        else
        {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    /**
     * Returns whether or not the specified data flavor is supported for this object.
     * 
     * @param flavor
     *          The DataFlavor type to check support for.
     * @return
     *          True if the DataFlavor is DataFlavor.imageFlavor.
     */
    public boolean isDataFlavorSupported (DataFlavor flavor)
    {
        return flavor == DataFlavor.imageFlavor;
    }

    /**
     * Returns an array of the supported DataFlavors.
     * 
     * @return
     *          Array of DataFlavors supported by this Transferable Object.
     */
    public DataFlavor[] getTransferDataFlavors ()
    {
        return new DataFlavor[] { DataFlavor.imageFlavor };
    }
}
