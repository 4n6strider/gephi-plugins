/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ox.oii.sigmaexporter.model;

/**
 *
 * @author shale
 */
public class GraphNode  extends GraphElement{
	
	private String label;
	private double x;
	private double y;
			
	public GraphNode(String id) {
		super(id);
		label="";
		size=1;
		x = 100 - 200*Math.random();
		y = 100 - 200*Math.random();
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	

}

