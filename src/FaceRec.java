import org.opencv.core.*;
import org.opencv.objdetect.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.*; // imread, imwrite, etc
import org.opencv.videoio.*;   // VideoCapture

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class FaceRec{
	
	// ** GLOBALS **
	
	// Haar cascade xml file names for later loading
	static String face_cascade_name = "haarcascade_frontalface_alt.xml";
	static String eyes_cascade_name = "haarcascade_eye_tree_eyeglasses.xml";
	
	// CascadeClassifier objects, from which "detectMultiScale" is used
	static CascadeClassifier face_cascade;
	static CascadeClassifier eyes_cascade;
	
	// Window for viewing, because Java's implementation throws a shit fit when trying to draw directly on-screen
    static JFrame f = new JFrame("FaceRec");
	
    public static void main(String[] args) {
    	
    	// Some methods require this
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// used to access webcam
		VideoCapture camera;
		
		// holds frames captured from camera
		Mat frame;
		
		// initialize cascade classifier objects
		face_cascade = new CascadeClassifier();
		eyes_cascade = new CascadeClassifier();
		
		// load Haar cascade files
		if( !face_cascade.load( face_cascade_name ) ){
			System.out.println("--(!)Error loading\n"); 
			System.exit(1); 
		};
		if( !eyes_cascade.load( eyes_cascade_name ) ){
			System.out.println("--(!)Error loading\n"); 
			System.exit(1);
		};
		
		// initializes camera, telling it to point to default webcam
		camera = new VideoCapture(0);
		
		// wait for camera to start up in case of lag. may need to tweak?
		try{
			Thread.sleep(1000);
		} catch (Exception e){
			e.printStackTrace();
		}
		
		// check if camera is connected
		if (camera != null){
			if(!camera.isOpened()){
				System.out.println("Camera connection failed");
				System.exit(1);
			}
			else System.out.println("Connection successful! " + camera.toString());
    	}
		
		// initialize frame object
		frame = new Mat();
		
		// set window layout, then open it
        f.getContentPane().setLayout(new FlowLayout());
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
        // begin loop
		while( true ){
			// grab frame from webcam
			camera.retrieve(frame);
		    
			// skip rendering until frames can be captured
			if (frame.empty()) continue;
			
			// otherwise, begin rendering
			detectAndDisplay( frame ); 
		 }
		
		 // unreachable without an event handler. no, i'm not going to write one.
		 // camera.release();
		 
	}
	
	public static void detectAndDisplay( Mat frame )
	{
	  // vector (aka fancy array) of faces found
	  MatOfRect faces = new MatOfRect();
	  
	  // grayscale version of frame for easier computation
	  Mat frame_gray = new Mat();
	  
	  // convert captured frame to grayscale
	  Imgproc.cvtColor( frame, frame_gray, Imgproc.COLOR_BGR2GRAY );
	  Imgproc.equalizeHist( frame_gray, frame_gray );

	  // detect faces
	  face_cascade.detectMultiScale( frame_gray, faces, 1.1, 2, 0 , new Size(30, 30), new Size());
	  
	  // turn faces vector into array, because OF COURSE the vector has no length attribute or getter method
	  Rect[] facesArr = faces.toArray();
	  
	  // for each face...
	  for( int i = 0; i < facesArr.length; i++ )
	  {
		// draw a circle around it
	    Point center = new Point( facesArr[i].x + facesArr[i].width*0.5, facesArr[i].y + facesArr[i].height*0.5 );
	    Imgproc.ellipse( frame, center, new Size( facesArr[i].width*0.5, facesArr[i].height*0.5), 0.0, 0.0, 360.0, new Scalar( 255, 0, 255 ), 4, 8, 0 );
	    
	    // detect eyes
	    Mat faceROI = frame_gray.submat(facesArr[i]);
	    MatOfRect eyes = new MatOfRect();
	    eyes_cascade.detectMultiScale( faceROI, eyes, 1.1, 2, 0 , new Size(30, 30), new Size());
	    
	    // convert eyes vector to array as well
	    Rect[] eyesArr = eyes.toArray(); 
	    
	    // draw circles around each "eye" found. [NOTE: not always necessarily eyes.]
	    for(int j = 0; j < eyesArr.length; j++)
	     {
	       Point center2 = new Point(facesArr[i].x + eyesArr[j].x + eyesArr[j].width*0.5, facesArr[i].y + eyesArr[j].y + eyesArr[j].height*0.5);
	       int radius = (int)Math.round((eyesArr[j].width + eyesArr[j].height)*0.25);
	       Imgproc.circle(frame, center2, radius, new Scalar( 255, 0, 0 ), 4, 8, 0);
	     }
	  }
	  // call custom made image showing
	  imshow(frame);
	 }
	
	public static void imshow(Mat src){
		// BufferedImage to display
	    BufferedImage bufImage = null;
	    try {
	    	
	    	// convert Mat to jpg for display
	        MatOfByte matOfByte = new MatOfByte();
	        Imgcodecs.imencode(".jpg", src, matOfByte); 
	        
	        // load processed jpg into BufferedImage
	        byte[] byteArray = matOfByte.toArray();
	        InputStream in = new ByteArrayInputStream(byteArray);
	        bufImage = ImageIO.read(in);
	        
	        // redraw image in animation loop
	        f.getContentPane().removeAll();
	        f.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
	        f.pack();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
