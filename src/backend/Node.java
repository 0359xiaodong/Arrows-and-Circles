package backend;

import java.awt.*;
import java.awt.geom.*;
import java.awt.geom.Point2D.Double;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import frontend.*;

/**
 * This is the node class. This contains data for both visual and logical representation of a node for FSMs/graphs.
 * @author ewald
 *
 */
public class Node implements DiagramObject, Cloneable {
	private Point2D.Double _center;
	private double _radius;
	private JTextField _area;
	private Collection<Edge> _connected;
	private Color _color;
	private boolean _startState;
	private boolean _endState;
	private Point _offset;
	private boolean _selected;
	private boolean _resizing;
	private boolean _current = false;
	private DrawingPanel _container;
	private JLabel _label;
	private Polygon _startSymbol;
	private java.awt.geom.Ellipse2D.Double _circle;

	public static final double MIN_RADIUS = 20;
	public static final double DEFAULT_RADIUS = 30;

	/**
	 * The constructor for the node takes the x,y to add the node to, and
	 * the DrawingPanel which contains this Node (used to add listeners
	 * to the Node's label).
	 * @param x
	 * @param y
	 * @param container
	 */
	public Node(double x, double y, DrawingPanel container) {
		_container = container;
		_center = new Point2D.Double(x, y);
		_radius = DEFAULT_RADIUS;
		_connected = new HashSet<Edge>();
		_color = Color.BLACK;
		_startState = false;
		_endState = false;
		_offset = new Point(0,0);
		_selected = true;

		setAreaAndLabel();
	}

	/**
	 * Second constructor used when opening from a file.
	 */
	public Node (double x, double y, double radius, boolean isStart, boolean isAccept, String label) {
		_center = new Point2D.Double(x, y);
		_radius = radius;
		_startState = isStart;
		_endState = isAccept;
		_color = Color.BLACK;
		_offset = new Point(0,0);
		_connected = new HashSet<Edge>();
		_selected = false;
		_label = new JLabel(label);
	}

	/**
	 * Helper for opening a Node from a file.
	 */
	public void setContainerAndLabel(DrawingPanel container) {
		_container = container;

		setAreaAndLabel();
	}

	/**
	 * Helper for both of the constructors. Sets up the JTextField used to edit the node's name, and the jlabel used to 
	 * display the name in HTML when the node is not selected.
	 */
	private void setAreaAndLabel() {
		double hypo = 2*_radius;
		double temp = hypo*hypo;
		double dimension = Math.sqrt(temp/2);
		_area = new JTextField();
		_area.setBorder(null);

		int size = _container.getDiagram().getNodes().size();
		String s = "q_"+ size;
		_area.setText(s);
		_area.setVisible(true);
		_area.setOpaque(false);
		_area.setSize((int)(dimension), 15);
		_area.setHorizontalAlignment(JTextField.CENTER);
		_area.selectAll();
		_area.setEditable(true);
		_area.setEnabled(true);
		_area.setBackground(new Color(0,0,0,0));
		if (_label == null || _label.equals(""))
			_label = new JLabel("<html>q<sub>"+size+"</sub>");
		_area.getDocument().addDocumentListener(new HTMLParser(_label));
		_area.addKeyListener(new EnterListener(_container, _area));
		_label.setVisible(true);
		_label.setOpaque(false);
		_label.setSize((int)(dimension), 15);
		_label.setHorizontalAlignment(JTextField.CENTER);

		_container.add(_label);
		_container.add(_area);

		_startSymbol = new Polygon();
		_startSymbol.addPoint((int)(this.getCenter().x - this.getRadius()),(int) (this.getCenter().y));
		_startSymbol.addPoint((int)(this.getCenter().x - this.getRadius() - 20),(int) (this.getCenter().y + 10));
		_startSymbol.addPoint((int)(this.getCenter().x - this.getRadius() - 20),(int) (this.getCenter().y - 10));
	}

	/**
	 * Returns the "start symbol" (a triangle) associated with this node for toggling start state.
	 */
	public Polygon getStartSymbol(){
		return _startSymbol;
	}

	/**
	 * Resets the start symbol to a new polygon.
	 */
	public void newStartSymbol(){
		_startSymbol = new Polygon();
	}

	/**
	 * Sets the center of the node to the x and y passed in.
	 * @param x
	 * @param y
	 */
	public void setCenter(double x, double y){
		_center.setLocation(x, y);
	}

	/**
	 * Returns the node's center as a point with double-precision coordinates.
	 * @return
	 */
	public Point2D.Double getCenter() {
		return _center;
	}

	/**
	 * Sets the node's "offset." This is used when dragging the node and is the offset from the mouse cursor to the
	 * center of the node. Without this information the node's center ends up being set to the cursor's location
	 * while dragging.
	 * @param x
	 * @param y
	 */
	public void setOffset(double x, double y){
		_offset.setLocation(x, y);
	}

	/**
	 * Returns the offset from node center to mouse cursor.
	 * @return
	 */
	public Point getOffset(){
		return _offset;
	}

	/**
	 * Sets the node's radius.
	 * @param r
	 */
	public void setRadius(double r){
		_radius = r;
	}

	/**	
	 * Returns the node's radius.
	 * @return
	 */
	public double getRadius() {
		return _radius;
	}

	/**
	 * Adds an edge to this node's collection of connected edges.
	 * @param e
	 * @return
	 */
	public boolean addConnected(Edge e){
		return _connected.add(e);
	}

	/**
	 * Removes an edge from this node's collections of connected edges.
	 * @param e
	 * @return
	 */
	public boolean removeConnected(Edge e){
		return _connected.remove(e);
	}

	/**
	 * Returns the collection of connected edges.
	 * @return
	 */
	public Collection<Edge> getConnected() {
		return _connected;
	}

	/**
	 * Returns whether the node is a start state.
	 * @return
	 */
	public boolean isStart(){
		return _startState;
	}

	/**
	 * Set this node to be a start state or not a start state.
	 * @param b
	 */
	public void setStart(boolean b){
		_startState = b;
	}

	/**
	 * Returns true if this node is an accept state, false otherwise.
	 * @return
	 */
	public boolean isEnd() {
		return _endState;
	}

	/**
	 * Set whether this node is an accept state.
	 * @param b
	 */
	public void setEnd(boolean b){
		_endState = b;
	}

	/**
	 * Returns this node's text field.
	 * @return
	 */
	public JTextField getTextField(){
		return _area;
	}

	/**
	 * Returns this node's jlabel.
	 * @return
	 */
	public JLabel getLabel(){
		return _label;
	}
	
	/**
	 * Returns a full clone of this node (as opposed to a shallow clone).
	 */
	public Object clone() throws CloneNotSupportedException {
		Node clonedObject = (Node) super.clone();
		clonedObject._center = (Double) _center.clone();
		clonedObject._radius = _radius;
		clonedObject._connected = new HashSet<Edge>();
		for(Edge e : _connected) {
			clonedObject._connected.add((Edge) e.clone());
		}
		clonedObject._startState = _startState;
		clonedObject._endState = _endState;
		return clonedObject;
	}


	/**
	 * Returns a rectangle to be used for resizing this node.
	 * @return
	 */
	public Rectangle2D getResize() {
		if (_circle == null) {
			resetCircle();
		}
		return new Rectangle2D.Double(_circle.x + 2*_radius,_circle.y+2*_radius,8,8);
	}

	/**
	 * Returns whether the node is currently being resized.
	 * @return
	 */
	public boolean resizing() {
		return _resizing;
	}
	
	/**
	 * Sets whether node is currently being resized.
	 * @param r
	 */
	public void setResizing(boolean r) {
		_resizing = r;
	}

	/**
	 * Returns the actual ellipse associated with the graphical representation of this node.
	 * @return
	 */
	public Ellipse2D.Double getCircle() {
		return _circle;
	}

	/**
	 * This method relocates the circle associated with this node based on its current location.
	 * It then relocates the other graphical components that move with the circle:
	 * the jlabel/textfield and start symbol triangle.
	 * @return
	 */
	public Ellipse2D.Double resetCircle() {
		double hypo = 2*_radius;
		double temp = hypo*hypo;
		double dimension = Math.sqrt(temp/2);
		Point p = new Point((int)(_center.x-(dimension/2)), (int)(_center.y-(dimension/2)));
		_area.setSize((int)(dimension), _area.getHeight());
		_label.setSize((int)(dimension), _label.getHeight());
		_area.setLocation(new Point(p.x+2, (int)(_center.y-6)));
		_label.setLocation(new Point(p.x+1, (int)(_center.y-6)));
		_circle = new Ellipse2D.Double(_center.x-_radius, _center.y-_radius, _radius*2, _radius*2);
		_startSymbol = new Polygon();
		_startSymbol.addPoint((int)(this.getCenter().x - this.getRadius()),(int) (this.getCenter().y));
		_startSymbol.addPoint((int)(this.getCenter().x - this.getRadius() - 20),(int) (this.getCenter().y + 10));
		_startSymbol.addPoint((int)(this.getCenter().x - this.getRadius() - 20),(int) (this.getCenter().y - 10));
		return _circle;
	}

	/**
	 * Returns true if and only if this node is currently selected.
	 * @return
	 */
	public boolean isSelected() {
		return _selected;
	}

	/**
	 * Sets whether node is currently selected.
	 * @param b
	 */
	public void setSelected(boolean b){
		_selected = b;
	}

	/**
	 * Sets whether node is the currently-highlighted node in simulation.
	 */
	public void setCurrent(boolean b) {
		_current = b;
	}

	/**
	 * Returns true if and only if node is currently highlighted in simulation.
	 */
	public boolean getCurrent() {
		return _current;
	}

	/**
	 * Returns the Node's name. This gives the text field's text (which includes underscores and \\<greek character> instead
	 * of subscripts and the characters themselves. This was the best solution we could find, since the simulation pane
	 * (where this name is ultimately displayed) does not support html.
	 */
	public String getName() {
		return ("Node: " + _area.getText());
	}



}