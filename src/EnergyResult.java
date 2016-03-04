
public class EnergyResult {
	private double total;
	private int min;
	private int max;
	private int[] seam;
	
	public EnergyResult(int[] s, double total){
		this.total =total;
		this.seam = s;
	}
	
	public EnergyResult(EnergyResult e){
		this.total = e.getTotalEnergy();
		this.seam = e.getSeam();
	}
	
	public double getTotalEnergy(){
		return total;
	}
	
	public int[] getSeam(){
		return seam;
	}

}
