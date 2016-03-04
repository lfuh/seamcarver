

/******************************************************************************
 *  Compilation:  javac Picture.java
 *  Execution:    java Picture imagename
 *  Dependencies: none
 *
 *  Data type for manipulating individual pixels of an image. The original
 *  image can be read from a file in jpg, gif, or png format, or the
 *  user can create a blank image of a given size. Includes methods for
 *  displaying the image in a window on the screen or saving to a file.
 *
 *  % java Picture mandrill.jpg
 *
 *  Remarks
 *  -------
 *   - pixel (x, y) is column x and row y, where (0, 0) is upper left
 *
 *   - see also GrayPicture.java for a grayscale version
 *
 ******************************************************************************/

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import java.util.*;


/**
 *  This class provides methods for manipulating individual pixels of
 *  an image. The original image can be read from a <tt>.jpg</tt>, <tt>.gif</tt>,
 *  or <tt>.png</tt> file or the user can create a blank image of a given size.
 *  This class includes methods for displaying the image in a window on
 *  the screen or saving it to a file.
 *  <p>
 *  Pixel (<em>x</em>, <em>y</em>) is column <em>x</em> and row <em>y</em>.
 *  By default, the origin (0, 0) is upper left, which is a common convention
 *  in image processing.
 *  The method <tt>setOriginLowerLeft()</tt> change the origin to the lower left.
 *  <p>
 *  For additional documentation, see
 *  <a href="http://introcs.cs.princeton.edu/31datatype">Section 3.1</a> of
 *  <i>Introduction to Programming in Java: An Interdisciplinary Approach</i>
 *  by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
public final class Picture implements ActionListener, ComponentListener {
    private BufferedImage image;               // the rasterized image
    private static JFrame frame;                      // on-screen view
    private String filename;                   // name of file
    private boolean isOriginUpperLeft = true;  // location of origin
    private int width, height;                 // width and height
    private int pre_width, pre_height;
    private static int origWidth, origHeight;

   /**
     * Initializes a blank <tt>width</tt>-by-<tt>height</tt> picture, with <tt>width</tt> columns
     * and <tt>height</tt> rows, where each pixel is black.
     *
     * @param width the width of the picture
     * @param height the height of the picture
     */
    public Picture(int width, int height) {
        if (width  < 0) throw new IllegalArgumentException("width must be nonnegative");
        if (height < 0) throw new IllegalArgumentException("height must be nonnegative");
        this.width  = width;
        this.height = height;
                
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // set to TYPE_INT_ARGB to support transparency
        filename = width + "-by-" + height;
    }

   /**
     * Initializes a new picture that is a deep copy of the argument picture.
     *
     * @param picture the picture to copy
     */
    public Picture(Picture picture) {
        width  = picture.width();
        height = picture.height();
                
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        filename = picture.filename;
        for (int col = 0; col < width(); col++)
            for (int row = 0; row < height(); row++)
                image.setRGB(col, row, picture.get(col, row).getRGB());
    }

   /**
     * Initializes a picture by reading from a file or URL.
     *
     * @param filename the name of the file (.png, .gif, or .jpg) or URL.
     */
    public Picture(String filename) {
        this.filename = filename;
        try {
            // try to read from file in working directory
            File file = new File(filename);
            if (file.isFile()) {
                image = ImageIO.read(file);
            }

            // now try to read from file in same directory as this .class file
            else {
                URL url = getClass().getResource(filename);
                if (url == null) {
                    url = new URL(filename);
                }
                image = ImageIO.read(url);
            }

            if (image == null) {
                throw new IllegalArgumentException("Invalid image file: " + filename);
            }

            width  = image.getWidth(null);
            height = image.getHeight(null);
            origWidth = width;
            origHeight = height;
        }
        catch (IOException e) {
            // e.printStackTrace();
            // throw new RuntimeException("Could not open file: " + filename);
        	show();
        }
    }

   /**
     * Initializes a picture by reading in a .png, .gif, or .jpg from a file.
     *
     * @param file the file
     */
    public Picture(File file) {
        try {
            image = ImageIO.read(file);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open file: " + file);
        }
        if (image == null) {
            throw new RuntimeException("Invalid image file: " + file);
        }
        width  = image.getWidth(null);
        height = image.getHeight(null);
        filename = file.getName();
    }

    public BufferedImage getImage() {
    	return image;
    }
    
    public void setImage(BufferedImage _image) {
    	image = _image;
    	width = image.getWidth(null);
    	height = image.getHeight(null);
    }
    
    public Graphics getGraphics() {
    	return image.getGraphics();
    }
    
    public BufferedImage getSubImage(int x, int y, int w, int h) {
    	return image.getSubimage(x, y, w, h);
    }
   /**
     * Returns a JLabel containing this picture, for embedding in a JPanel,
     * JFrame or other GUI widget.
     *
     * @return the <tt>JLabel</tt>
     */
    public JLabel getJLabel() {
        if (image == null) return null;         // no image available
        ImageIcon icon = new ImageIcon(image);
        return new JLabel(icon);
    }

   /**
     * Sets the origin to be the upper left pixel. This is the default.
     */
    public void setOriginUpperLeft() {
        isOriginUpperLeft = true;
    }

   /**
     * Sets the origin to be the lower left pixel.
     */
    public void setOriginLowerLeft() {
        isOriginUpperLeft = false;
    }
    
    public void setPrevWH(int w, int h){
    	pre_width = w;
    	pre_height = h;
    }
    
    public void restoreImage(){
    	try {
            // try to read from file in working directory
    		if(origWidth == 0 && origHeight==0)
    			return;
            File file = new File(filename);
            if (file.isFile()) {
                image = ImageIO.read(file);
            }

            // now try to read from file in same directory as this .class file
            else {
                URL url = getClass().getResource(filename);
                if (url == null) {
                    url = new URL(filename);
                }
                image = ImageIO.read(url);
            }

            if (image == null) {
                throw new IllegalArgumentException("Invalid image file: " + filename);
            }

            width  = image.getWidth(null);
            height = image.getHeight(null);
            origWidth = width;
            origHeight = height;
        }
        catch (IOException e) {
            // e.printStackTrace();
            throw new RuntimeException("Could not open file: " + filename);
        }
    	redraw();
    }

   /**
     * Displays the picture in a window on the screen.
     */
    public void show() {

        // create the GUI for viewing the image if needed
        if (frame == null) {
            frame = new JFrame();

            JMenuBar menuBar = new JMenuBar();
            JMenu menu = new JMenu("File");
            menuBar.add(menu);
            JMenuItem menuItem1 = new JMenuItem(" Save...   ");
            menuItem1.addActionListener(this);
            menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            menuItem1.setActionCommand("Save");
            
            
            JMenuItem menuItem2 = new JMenuItem(" Open...   ");
            menuItem2.addActionListener(this);
            menuItem2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            menuItem2.setActionCommand("Open");
            menu.add(menuItem2);
            menu.add(menuItem1);
            frame.setJMenuBar(menuBar);

            frame.addComponentListener(this);	

            frame.setContentPane(getJLabel());
            // f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setTitle(filename);
            frame.setResizable(true);
            frame.pack();
            frame.setVisible(true);
        }

        // draw
        frame.repaint();
    }

    public void redraw(){
    	frame.setContentPane(getJLabel());
    	frame.pack();
        frame.setVisible(true);
    	frame.repaint();
    }
   /**
     * Returns the height of the picture.
     *
     * @return the height of the picture (in pixels)
     */
    public int height() {
        return height;
    }

   /**
     * Returns the width of the picture.
     *
     * @return the width of the picture (in pixels)
     */
    public int width() {
        return width;
    }

   /**
     * Returns the color of pixel (<tt>col</tt>, <tt>row</tt>).
     *
     * @param col the column index
     * @param row the row index
     * @return the color of pixel (<tt>col</tt>, <tt>row</tt>)
     * @throws IndexOutOfBoundsException unless both 0 &le; <tt>col</tt> &lt; <tt>width</tt>
     *         and 0 &le; <tt>row</tt> &lt; <tt>height</tt>
     */
    public Color get(int col, int row) {
        if (col < 0 || col >= width())  throw new IndexOutOfBoundsException("col must be between 0 and " + (width()-1));
        if (row < 0 || row >= height()) throw new IndexOutOfBoundsException("row must be between 0 and " + (height()-1));
        if (isOriginUpperLeft) return new Color(image.getRGB(col, row));
        else                   return new Color(image.getRGB(col, height - row - 1));
    }

   /**
     * Sets the color of pixel (<tt>col</tt>, <tt>row</tt>) to given color.
     *
     * @param col the column index
     * @param row the row index
     * @param color the color
     * @throws IndexOutOfBoundsException unless both 0 &le; <tt>col</tt> &lt; <tt>width</tt>
     *         and 0 &le; <tt>row</tt> &lt; <tt>height</tt>
     * @throws NullPointerException if <tt>color</tt> is <tt>null</tt>
     */
    public void set(int col, int row, Color color) {
        if (col < 0 || col >= width())  throw new IndexOutOfBoundsException("col must be between 0 and " + (width()-1));
        if (row < 0 || row >= height()) throw new IndexOutOfBoundsException("row must be between 0 and " + (height()-1));
        if (color == null) throw new NullPointerException("can't set Color to null");
        if (isOriginUpperLeft) image.setRGB(col, row, color.getRGB());
        else                   image.setRGB(col, height - row - 1, color.getRGB());
    }

   /**
     * Returns true if this picture is equal to the argument picture.
     *
     * @param other the other picture
     * @return <tt>true</tt> if this picture is the same dimension as <tt>other</tt>
     *         and if all pixels have the same color; <tt>false</tt> otherwise
     */
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        Picture that = (Picture) other;
        if (this.width()  != that.width())  return false;
        if (this.height() != that.height()) return false;
        for (int col = 0; col < width(); col++)
            for (int row = 0; row < height(); row++)
                if (!this.get(col, row).equals(that.get(col, row))) return false;
        return true;
    }

    /**
     * This operation is not supported because pictures are mutable.
     *
     * @return does not return a value
     * @throws UnsupportedOperationException if called
     */
    public int hashCode() {
        throw new UnsupportedOperationException("hashCode() is not supported because pictures are mutable");
    }

   public void open(String filename){
	   this.filename = filename;
       try {
           // try to read from file in working directory
           File file = new File(filename);
           if (file.isFile()) {
               image = ImageIO.read(file);
           }

           // now try to read from file in same directory as this .class file
           else {
               URL url = getClass().getResource(filename);
               if (url == null) {
                   url = new URL(filename);
               }
               image = ImageIO.read(url);
           }

           if (image == null) {
               throw new IllegalArgumentException("Invalid image file: " + filename);
           }

           width  = image.getWidth(null);
           height = image.getHeight(null);
           origWidth = width;
           origHeight = height;
       }
       catch (IOException e) {
           // e.printStackTrace();
           throw new RuntimeException("Could not open file: " + filename);       	
       }
       redraw();
   }
   /**
     * Saves the picture to a file in a standard image format.
     * The filetype must be .png or .jpg.
     *
     * @param name the name of the file
     */
    public void save(String name) {
        save(new File(name));
    }

   /**
     * Saves the picture to a file in a PNG or JPEG image format.
     *
     * @param file the file
     */
    public void save(File file) {
        filename = file.getName();
        if (frame != null) frame.setTitle(filename);
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);
        suffix = suffix.toLowerCase();
        if (suffix.equals("jpg") || suffix.equals("png")) {
            try {
                ImageIO.write(image, suffix, file);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Error: filename must end in .jpg or .png");
        }
    }

   /**
     * Opens a save dialog box when the user selects "Save As" from the menu.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    	
    	if(e.getActionCommand() == "Save"){
	        FileDialog chooser = new FileDialog(frame,
	                             "Use a .png or .jpg extension", FileDialog.SAVE);
	        chooser.setVisible(true);
	        if (chooser.getFile() != null) {
	            save(chooser.getDirectory() + File.separator + chooser.getFile());
	        }
    	}
    	else
    	{
    		FileDialog chooser = new FileDialog(frame,
                    "Use a .png or .jpg extension", FileDialog.LOAD);
			chooser.setVisible(true);
			if (chooser.getFile() != null) {
			   open(chooser.getDirectory() + File.separator + chooser.getFile());
			}
    	}
    }


    /* component implementation */
    public void componentHidden(ComponentEvent event)
    {};

    public void componentResized(ComponentEvent event)
    {        
        System.out.printf("%d-by-%d\n", frame.getContentPane().getWidth(),frame.getContentPane().getHeight());
        SeamCarver seamcarver = new SeamCarver(this);
        boolean redraw = false;
        long t = System.currentTimeMillis();

        if( frame.getContentPane().getWidth() < this.pre_width)
        {
        	int diff = this.pre_width - frame.getContentPane().getWidth();
        	for(int i=0;i<diff;i++){
		        try {
					seamcarver.removeVerticalSeam(seamcarver.findVerticalSeam());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        this.setImage(seamcarver.image());
	        }
        	redraw = true;
        }
        if( frame.getContentPane().getHeight() < this.pre_height)
        {
        	int diff = this.pre_height - frame.getContentPane().getHeight();
        	for(int i=0;i<diff;i++){
		        try {
					seamcarver.removeHorizontalSeam(seamcarver.findHorizontalSeam());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        this.setImage(seamcarver.image());
	        }
        	redraw = true;
        }
        
        if(frame.getContentPane().getWidth() > origWidth && frame.getContentPane().getHeight() > origHeight){        
        	restoreImage();
        }
        setPrevWH(width,height);
        System.out.printf("Elapse Time:%d\n", System.currentTimeMillis()-t);
        if(redraw)
        	this.redraw();
                
    };

    public void componentShown(ComponentEvent event)
    {};

    public void componentMoved(ComponentEvent event)
    {    	
    };

    
    
   /**
     * Unit tests this <tt>Picture</tt> data type.
     * Reads a picture specified by the command-line argument,
     * and shows it in a window on the screen.
 * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
    	Picture picture = null;
    	if(args.length !=0)
         picture = new Picture(args[0]);
    	else
    	 picture = new Picture(100,100);
    	
        System.out.printf("%d-by-%d\n", picture.width(), picture.height());        
        picture.setPrevWH(picture.width(),picture.height());
        picture.show();        
                
        SeamCarver seamcarver = new SeamCarver(picture);
        
        seamcarver.removeHorizontalSeam(seamcarver.findHorizontalSeam());
        seamcarver.removeHorizontalSeam(seamcarver.findHorizontalSeam());
        seamcarver.removeHorizontalSeam(seamcarver.findHorizontalSeam());
        seamcarver.removeHorizontalSeam(seamcarver.findHorizontalSeam());
        
        
    }

}

