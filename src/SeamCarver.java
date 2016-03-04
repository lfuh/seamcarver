import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class SeamCarver {

   private Picture p;
   Map<Integer,Double> top_energy;
   Map<Integer,Double> sorted_top_energy;   
   double cache[][];
   
   public static <K, V extends Comparable<? super V>> Map<K, V> seamcarverSortMap(final Map<K, V> mapToSort) {
		List<Map.Entry<K, V>> entries = new ArrayList<Map.Entry<K, V>>(mapToSort.size());

		entries.addAll(mapToSort.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(final Map.Entry<K, V> entry1, final Map.Entry<K, V> entry2) {
				return entry1.getValue().compareTo(entry2.getValue());
			}
		});

		Map<K, V> sortedseamcarverMap = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : entries) {
			sortedseamcarverMap.put(entry.getKey(), entry.getValue());
		}
		return sortedseamcarverMap;
	}
   
   public Map<Integer,Double> getTopEngeryArray(){
	   return sorted_top_energy;
   }
   
   public SeamCarver(Picture picture)                // create a seam carver object based on the given picture
   {
	   this.p = picture;
	   
	   cache = new double[p.width()][p.height()];
   }
   
   
   
   public Picture picture()                          // current picture
   {
	   return p;
   }
   
   public BufferedImage image()                          // current picture
   {
	   return p.getImage();
   }
   
   public     int width()                            // width of current picture
   {
	   return p.width();
   }
   
   public     int height()                           // height of current picture
   {
	   return p.height();
   }
   
   //given 2 pixel, return the delta square of the RGB value
   private double deltaSquare(Color p1, Color p2){
	   double v = 0;
	   
		   v = Math.pow((p1.getRed()-p2.getRed()),2) +
			   Math.pow((p1.getGreen()-p2.getGreen()),2) +
			   Math.pow((p1.getBlue()-p2.getBlue()),2);		
	   
	   return v;
   }
   
   public  double energy(int x, int y)               // energy of pixel at column x and row y
   {
	   double e = 0;	   
	   Color p1, p2;
	   double dX,dY;	   
	   
	   if(x > width()-1 || y > height()-1 || x < 0 || y < 0){
		   System.out.printf("Exception: x:%d. y:%d\n",x,y);
		   throw new IndexOutOfBoundsException("out of range !");
	   }
	   
	   if(cache[x][y]!=0)
		   return cache[x][y];
	   //find x delta square
	   //check special boundary case
	   if(x == 0){
		   p1 = p.get(width()-1, y);
		   p2 = p.get(1, y);
	   }
	   else if ( x == width()-1){
		   p1 = p.get(x-1, y);
	   	   p2 = p.get(0, y);
       }
   	   else{
   		   p1 = p.get(x-1,y);
   		   p2 = p.get(x+1,y);
   	   }
	   dX = deltaSquare(p1,p2);
	   
	   //find y delta square
	   //check special boundary case
	   if(y == 0){
		   p1 = p.get(x,height()-1);
		   p2 = p.get(x, 1);
	   }
	   else if ( y == height()-1){
		   p1 = p.get(x, y-1);
	   	   p2 = p.get(x, 0);
       }
   	   else{
   		   p1 = p.get(x,y-1);
   		   p2 = p.get(x,y+1);
   	   }
	   dY = deltaSquare(p1,p2);
	   
	   e = dX + dY;
	   cache[x][y] = e;
	   
	   return e;	   	   
   }
   
   
   private int findMinimunIndexArray(double[] value){
	   
	   int min = 1;
	   	   	   
	   if(value[min] > value[2]){
			   min = 2;		   
	   }
	   if( value[min] > value[0]){
		   min = 0;
	   }
	   //System.out.printf("value: %f,%f,%f,  minimun index:%d\n",value[0],value[1],value[2],min);
	   return min;
   }
   
   public EnergyResult findHorizontalSeam(int row)                 // sequence of indices for vertical seam
   {
	   double e[]={Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE};
	   int y = row;
	   double total = 0;
	   int index = 0;
	   int v[] = new int[p.width()];
	   
	   v[0] = y;
	   //start from the left.
	   for(int i=1;i<p.width();i++){
		   if(y-1 >= 0)			   
			   e[0] = this.energy(i,y-1);
		   else
			   e[0] = Double.MAX_VALUE;
		   
		   e[1] = this.energy(i,y);
		   
		   if(y+1 < p.height())
			   e[2] = this.energy(i,y+1);
		   else
			   e[2] = Double.MAX_VALUE;
		   
		   //System.out.printf("value: %d,%d:%f, %d,%d:%f, %d,%d:%f\n",x-1,i,e[0],x,i,e[1],x+1,i,e[2]);
		   index = findMinimunIndexArray(e);
		   y += index-1;
		   total += e[index];
		   v[i] = y;
		   	   
	   }
	   
	   EnergyResult result = new EnergyResult(v,total);
	   
	   return result;
   
   }
   
   public int threadNo = 4;
   public boolean profile=false;
   public   int[] findHorizontalSeam() throws InterruptedException               // sequence of indices for horizontal seam
   {	   
	   long t = System.currentTimeMillis();	   
	   EnergyResult r = null, c=null;
	   	   	   
	   if(threadNo==0){	   
		   r = findHorizontalSeam(0);
		   for(int i=0;i<p.height();i++){
			   c = findHorizontalSeam(i);
			   if(c.getTotalEnergy() < r.getTotalEnergy()){
				   r = c;
			   }		  
		   }
	   }
	   else{
		   class HorizontalTasker implements Callable<EnergyResult> {
			   private int start;
			   private int stop;
			   
			   public HorizontalTasker(int a, int b){ start=a;stop=b;}
			   
			   public EnergyResult call(){
				   EnergyResult _min;	   
				   EnergyResult _c;
				   
				   _min = findHorizontalSeam(start);
				   for(int i=start;i<stop;i++){
					   _c = findHorizontalSeam(i);
					   if(_c.getTotalEnergy() < _min.getTotalEnergy()){
						   _min = _c;
					   }				  
				   }
				   return _min;
			   }
		   }
		   
		   try{
			   
			   ExecutorService executorService = Executors.newFixedThreadPool(threadNo);
			   
			   List<Callable<EnergyResult>> lst = new ArrayList<Callable<EnergyResult>>();
			   
			   int divide = p.height()/threadNo;
			   
			   for(int i=0;i<threadNo;i++){
				   if(i==0)
					   lst.add( new HorizontalTasker(0,(i+1)*divide));			   
				   else if(i==threadNo-1)
					   lst.add( new HorizontalTasker(i*divide+1,(i+1)*divide));
				   else
					   lst.add( new HorizontalTasker(i*divide+1,p.height()));
			   }		   
			   
			   List<Future<EnergyResult>> future = executorService.invokeAll(lst);
			   
			   r = future.get(0).get();
			   for(Future<EnergyResult> item : future){
				   if(item.get().getTotalEnergy() < r.getTotalEnergy())
					   r = item.get(); 
			   }
			   /* shutdown your thread pool, else your application will keep running */
		       executorService.shutdown();
			   		   
		   }catch (ExecutionException e){
		   
		   }
	   }
	   if(profile)
		   System.out.printf("findHorizontalSeam Elapse Time:%d\n", System.currentTimeMillis()-t);
	   return r.getSeam();
   }
   
   
   public EnergyResult findVerticalSeam(int col)                 // sequence of indices for vertical seam
   {	   
	   double e[]={Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE};
	   int x = col;
	   double total = 0;
	   int index = 0;
	   int v[] = new int[p.height()];
	   
	   v[0] = x;
	   //start from the top.
	   for(int i=1;i<p.height();i++){
		   if(x-1 >= 0)			   
			   e[0] = this.energy(x-1,i);
		   else
			   e[0] = Double.MAX_VALUE;
		   
		   e[1] = this.energy(x,i);
		   
		   if(x+1 < p.width())
			   e[2] = this.energy(x+1,i);
		   else
			   e[2] = Double.MAX_VALUE;
		   
		   //System.out.printf("value: %d,%d:%f, %d,%d:%f, %d,%d:%f\n",x-1,i,e[0],x,i,e[1],x+1,i,e[2]);
		   index = findMinimunIndexArray(e);
		   x += index-1;
		   total += e[index];
		   v[i] = x;
		   	   
	   }
	   
	   EnergyResult result = new EnergyResult(v,total);
	   
	   return result;
   }
   
   
   public   int[] findVerticalSeam() throws InterruptedException                // sequence of indices for vertical seam
   {
	   EnergyResult r = null, c=null;
	   long t = System.currentTimeMillis();	   
	   
	   if(threadNo==0){	   
		   r = findVerticalSeam(0);
		   for(int i=0;i<p.width();i++){
			   c = findVerticalSeam(i);
			   if(c.getTotalEnergy() < r.getTotalEnergy()){
				   r = c;
			   }
			   
		   }
	   }
	   else{
		   class VerticalTasker implements Callable<EnergyResult> {
			   private int start;
			   private int stop;
			   
			   public VerticalTasker(int a, int b){ start=a;stop=b;}
			   
			   public EnergyResult call(){
				   EnergyResult _min;	   
				   EnergyResult _c;
				   
				   _min = findVerticalSeam(start);
				   for(int i=start;i<stop;i++){
					   _c = findVerticalSeam(i);
					   if(_c.getTotalEnergy() < _min.getTotalEnergy()){
						   _min = _c;
					   }				  
				   }
				   return _min;
			   }
		   }
		   
		   try{
			   
			   ExecutorService executorService = Executors.newFixedThreadPool(threadNo);
			   
			   List<Callable<EnergyResult>> lst = new ArrayList<Callable<EnergyResult>>();
			   
			   int divide = p.width()/threadNo;
			   
			   for(int i=0;i<threadNo;i++){
				   if(i==0)
					   lst.add( new VerticalTasker(0,(i+1)*divide));			   
				   else if(i==threadNo-1)
					   lst.add( new VerticalTasker(i*divide+1,(i+1)*divide));
				   else
					   lst.add( new VerticalTasker(i*divide+1,p.width()));
			   }		   
			   
			   List<Future<EnergyResult>> future = executorService.invokeAll(lst);
			   
			   r = future.get(0).get();
			   for(Future<EnergyResult> item : future){
				   if(item.get().getTotalEnergy() < r.getTotalEnergy())
					   r = item.get(); 
			   }
			   /* shutdown your thread pool, else your application will keep running */
		       executorService.shutdown();
			   		   
		   }catch (ExecutionException e){
		   
		   }
	   }
	   if(profile)
		   System.out.printf("findVerticalSeam Elapse Time:%d\n", System.currentTimeMillis()-t);
	   return r.getSeam();
   }
   
   
   
   public void removeHorizontalSeam(int [] seam)     // remove horizontal seam from current picture
   {
	   Picture newPic = new Picture(p.width(),p.height()-1);
	   int row=0;
	   Graphics newG = newPic.getGraphics();
	   long t = System.currentTimeMillis();
	   
	   	   
	   for(int x=0; x<p.width();x++){
		   if(seam[x]>0){
			   BufferedImage a = p.getSubImage(x, 0, 1,seam[x]);		   
			   newG.drawImage(a, x, 0, 1, seam[x], null);
		   }
		   
		   if(p.height()-1-seam[x] >0){
			   BufferedImage b = p.getSubImage(x,seam[x]+1, 1, p.height()-1-seam[x]);
			   newG.drawImage(b, x, seam[x], 1, p.height()-1-seam[x], null);
		   }
	   }
	   
	   this.p = newPic;
	   cache = new double[p.width()][p.height()];
	   if(profile)
		   System.out.printf("removeHorizontalSeam Elapse Time:%d\n", System.currentTimeMillis()-t);
   }
   
   
   
   public    void removeVerticalSeam(int[] seam)     // remove vertical seam from current picture
   {
	   Picture newPic = new Picture(p.width()-1,p.height());
	   int col=0;
	   Graphics newG = newPic.getGraphics();
	   long t = System.currentTimeMillis();
	   	   
	   for(int y=0; y<p.height();y++){
		   if(seam[y]>0){
			   BufferedImage a = p.getSubImage(0, y, seam[y], 1);		   
			   newG.drawImage(a, 0, y, seam[y], 1, null);
		   }
		   
		   if(p.width()-1-seam[y] >0){
			   BufferedImage b = p.getSubImage(seam[y]+1, y, p.width()-1-seam[y], 1);
			   newG.drawImage(b, seam[y], y, p.width()-1-seam[y], 1, null);
		   }
	   }
	   
	   this.p = newPic;
	   cache = new double[p.width()][p.height()];
	   if(profile)
		   System.out.printf("removeVerticalSeam Elapse Time:%d\n", System.currentTimeMillis()-t);
   }
}
