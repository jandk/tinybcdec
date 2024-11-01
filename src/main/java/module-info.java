/**
 * This module provides a decoder for block compressed textures.
 * <p>
 * For now the following formats are supported:
 * <ul>
 *     <li>BC1</li>
 *     <li>BC2</li>
 *     <li>BC3</li>
 *     <li>BC4</li>
 *     <li>BC5</li>
 *     <li>BC6H</li>
 *     <li>BC7</li>
 * </ul>
 */
module be.twofold.tinybcdec {
    // Optional, for Converter
    requires static java.desktop;
    requires static javafx.graphics;

    exports be.twofold.tinybcdec;
}
