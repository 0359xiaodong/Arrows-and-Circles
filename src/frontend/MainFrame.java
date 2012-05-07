/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package frontend;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import backend.*;

/**
 *
 * @author Eddie, Sandy, Eli, and Plane
 */
public class MainFrame extends javax.swing.JFrame {

	/*
	 * Instance variables of the MainFrame. Refer to the guide below:
	 * 
	 * _edgeStart is set to be the start node of the edge currently being drawn.  When
	 * 		the mouse is moved over a node, it is set as _edgeStart, so that when shift
	 * 		is pressed and the mouse is dragged, we know where to begin the progress line.
	 * 
	 * _resizing is the currently selected resizing box on a node.  If a resizing box
	 * 		is pressed, then this variable is set, so that when the mouse is dragged,
	 * 		we know which box was pressed.
	 * 
	 * _mouseLoc is updated every time the mouse is moved, and represents the current mouse
	 * 		location on the screen.
	 * 
	 * _robot is the Robot used to move the user's mouse when "s" is pressed.
	 * 
	 * _nodesSelected is the set of currently selected nodes in the diagram.  Make sure to
	 * 		call Collections.synchronizedSet if you need to re-instantiate this, because it
	 * 		is possible that two separate methods will try to access this set at once.
	 * 
	 * _edgesSelected is the currently selected edges, and has the same note as above.
	 * 
	 * _sim is the List returned by simulating the current FSM.  Used in all the simulation
	 * 		methods down towards the bottom of this file.
	 * 
	 * _iter is the Iterator used to iterate through the _sim list.
	 * 
	 * _simTimer is the timer used in simulation to step from one step in the simulation
	 * 		to the next.
	 * 
	 * _edgeType represents the current type of edge selected, so that when we make a new
	 * 		edge, we know what type of edge to create.
	 * 
	 * _simSlide is the Slider used to iterate through the simulation.  It is the bottom
	 * 		slider, and can be dragged forward or backward to step through the simulation.
	 * 
	 * _curr NOT SURE WHAT THIS DOES; EDDIE MADE THIS.
	 * 
	 * _autoChange is used when we want to change the slider's value without having
	 * 		to go through the slider changed method; used for the second slider.  If you want
	 * 		to change the value of the second slider without stopping simulation, set this to
	 * 		true, set the value of the slider, then reset it to false.
	 * 
	 * _backwardClicked is used in the interplay between clicking forward and backward.  If you
	 * 		make alternating calls to an Iterator's previous() and next(), the same object will
	 * 		be returned every time.  Thus this variable is used so that if we want to go to the
	 * 		next object after having just gone backwards, we will call next() one extra time;
	 * 		similarly, if we want to go backwards after calling next(), we have to call previous()
	 * 		one extra time.  If this variable is FALSE, then we are currently stepping BACKWARDS;
	 * 		similarly, if it is TRUE, we are currently stepping FORWARDS.  This is used in the
	 * 		simulation_move_forward/backward methods.
	 * 
	 * _currentInputString is used in simulation to alert the user when the input string changes
	 * 		during simulation.  It gets set the first time simulation starts, and then each time
	 * 		the simulation moves forwards or backwards, it checks to make sure that whatever is
	 * 		in the text field is equal to this instance variable.  Otherwise it alerts the user
	 * 		and gives them the opportunity to restart.
	 * 
	 * _helpText is the label displayed at the bottom of the main frame to alert the user to
	 * 		keybindings.
	 * 
	 * _shiftClicked is false if shift is not currently being held, and true otherwise.  We
	 * 		need this variable because Mac OS doesn't recognize shift in Mouse Modifiers for
	 * 		some reason.  TAKING THIS OUT FOR NOW.
	 * 
	 * _selectRectangle is the rectangle used to illustrate click-and-drag selction.
	 * 
	 * _selectPoint is the starting point of _selectRectangle.
	 */
	private Node _edgeStart;
	private Node _resizing;
	private Point _mouseLoc;
	private Robot _robot;
	private Collection<Node> _nodesSelected;
	private Collection<Edge> _edgesSelected;
	private Node _nodeDragged;
	private Edge _edgeDragged;
	private List<DiagramObject> _sim;
	private ListIterator<DiagramObject> _iter;
	private Timer _simTimer;
	private EdgeDirection _edgeType;
	private javax.swing.JSlider _simSlide;
	private boolean _autoChange;
	private boolean _backwardClicked;
	private String _currentInputString;
	private JLabel _helpText;
	private Point _selectPoint;

	//The file paths of image resources, and other global static variables we want to define.
	private static final String PLAY_FILEPATH = "./src/img/play.png";
	private static final String PAUSE_FILEPATH = "./src/img/pause.png";
	private static final String FWD_FILEPATH = "./src/img/fwd.png";
	private static final String BWD_FILEPATH = "./src/img/bwd.png";
	private static final String STOP_FILEPATH = "./src/img/stop.png";
	
	private static final String help_message_text = "Double Click: New Node | \"S\": Snap Mouse To Nearest Node | Ctrl-Click Component: Add Component To Selected Components";
	private static final String help_message_text_in_node_unselected = "Click To Select | Double Click: Toggle Accept State | Shift-DragClick: New Edge | Click-Drag: Move It Around";
	private static final String help_message_text_in_node_selected = "Shift-DragClick: New Edge | Double Click: Toggle Accept | Delete/Backspace: Delete Node | Click Triangle: Toggle Start State";
	private static final String help_messate_text_in_edge = "To Delete: If Text Is Selected, Hit Enter; Then Delete | Direction: Lower Left Pane | Multiple Labels: Comma Separated";
	
	//If we are within 3 pixels of another node, then snap to that node.
	public static final int SNAP_DIFFERENCE = 7;
	
	//Zooming scale
	private static final int CANVAS_WIDTH = 1024;
	private static final int CANVAS_HEIGHT = 1024;

	//Max Value of the Sim Slider
	private static final int MAX_SIM_SLIDER_VAL = 1000;
	
	/*
	 * These are the GUI components.
	 */
	private javax.swing.JRadioButton _doublyBtn;
	private javax.swing.ButtonGroup _edgeTypeGrp;
	private javax.swing.JButton _forwardBtn;
	private javax.swing.JButton _playPauseBtn;
	private javax.swing.JButton _rewindBtn;
	private javax.swing.JRadioButton _singlyBtn;
	private javax.swing.JSlider _speedSlider;
	private javax.swing.JButton _stopBtn;
	private javax.swing.JRadioButton _undirectedBtn;
	private frontend.DrawingPanel drawingPanel1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JMenu jMenu3;
	private javax.swing.JMenu jMenu4;
	private javax.swing.JMenu jMenuTools;
	private javax.swing.JMenu jMenuHelp;
	private javax.swing.JMenuBar jMenuBar2;
	private javax.swing.JMenuItem jMenuItemRedo;
	private javax.swing.JMenuItem jMenuItem3;
	private javax.swing.JMenuItem jMenuItem4;
	private javax.swing.JMenuItem jMenuItem5;
	private javax.swing.JMenuItem jMenuItem6;
	private javax.swing.JMenuItem jMenuItem7;
	private javax.swing.JMenuItem jMenuItem8;
	private javax.swing.JMenuItem jMenuItemUndo;
	private javax.swing.JMenuItem jMenuItemSelectAll;
	private javax.swing.JMenuItem jMenuItemShowTrans;
	private javax.swing.JMenuItem jMenuItemAbout;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JSplitPane jSplitPane2;
	private javax.swing.JSplitPane jSplitPane3;
	private javax.swing.JTabbedPane jTabbedPane1;
	private javax.swing.JTextArea jTextArea1;
	private javax.swing.JTextField jTextField1;

	/**
	 * Creates new form MainFram.  Initializes the default edge type to SINGLE, and initiates the Timer.
	 * Sets backwardClicked to false, because initially we assume we want to go forward first in simulation.
	 * 
	 */
	public MainFrame() {
		_edgeType = EdgeDirection.SINGLE;
		_simTimer = new Timer(1000, new SimListener());
		_autoChange = false;
		_backwardClicked = false;
		_helpText = new JLabel();
		_helpText.setHorizontalAlignment(JLabel.CENTER);
		try {
			_robot = new Robot();
		} catch (AWTException ex) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
		}
		initComponents();
	}

	/**
	 * The initComponents method is the main method which initializes all components of the GUI.
	 * It adds all necessary listeners, creates all necessary GUI components, and formats them
	 * on the main Canvas.
	 */                
	private void initComponents() {

		_edgeTypeGrp = new javax.swing.ButtonGroup();
		jSplitPane2 = new javax.swing.JSplitPane();
		jTabbedPane1 = new javax.swing.JTabbedPane();
		jScrollPane1 = new javax.swing.JScrollPane();
		drawingPanel1 = new frontend.DrawingPanel();
		jSplitPane3 = new javax.swing.JSplitPane();
		jPanel1 = new javax.swing.JPanel();
		_singlyBtn = new javax.swing.JRadioButton();
		_doublyBtn = new javax.swing.JRadioButton();
		_undirectedBtn = new javax.swing.JRadioButton();
		jLabel2 = new javax.swing.JLabel();
		jPanel2 = new javax.swing.JPanel();
		jScrollPane2 = new javax.swing.JScrollPane();
		jTextArea1 = new javax.swing.JTextArea();
		jTextField1 = new javax.swing.JTextField();
		_speedSlider = new javax.swing.JSlider();
		_simSlide = new javax.swing.JSlider();
		_rewindBtn = new javax.swing.JButton();
		_stopBtn = new javax.swing.JButton();
		_playPauseBtn = new javax.swing.JButton();
		_forwardBtn = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();
		jMenuBar2 = new javax.swing.JMenuBar();
		jMenu3 = new javax.swing.JMenu();
		jMenuItem3 = new javax.swing.JMenuItem();
		jMenuItem4 = new javax.swing.JMenuItem();
		jMenuItem5 = new javax.swing.JMenuItem();
		jMenuItem7 = new javax.swing.JMenuItem();
		jMenuItem6 = new javax.swing.JMenuItem();
		jMenuItem8 = new javax.swing.JMenuItem();
		jMenu4 = new javax.swing.JMenu();
		jMenuItemUndo = new javax.swing.JMenuItem();
		jMenuItemRedo = new javax.swing.JMenuItem();
		jMenuItemSelectAll = new javax.swing.JMenuItem();
		jMenuItemShowTrans = new javax.swing.JMenuItem();
		jMenuItemAbout = new javax.swing.JMenuItem();
		jMenuTools = new javax.swing.JMenu();
		jMenuHelp = new javax.swing.JMenu();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Arrows & Circles");

		jTabbedPane1.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent evt){
				if (((JTabbedPane)evt.getSource()).getSelectedComponent() != null) {
					jScrollPane1 = (JScrollPane)((JTabbedPane)evt.getSource()).getSelectedComponent();
					drawingPanel1 = (DrawingPanel)jScrollPane1.getViewport().getView();
				}
			}
		});

		drawingPanel1.addMouseListener(new DrawingPanelMouseListener(this));
		drawingPanel1.addMouseMotionListener(new DrawingPanelMouseMotionListener(this));
		drawingPanel1.addKeyListener(new DrawingPanelKeyListener(this));

		javax.swing.GroupLayout drawingPanel1Layout = new javax.swing.GroupLayout(drawingPanel1);
		drawingPanel1.setLayout(drawingPanel1Layout);
		drawingPanel1.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

		jScrollPane1.setViewportView(drawingPanel1);
		jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
		
		jTabbedPane1.addTab("Untitled", jScrollPane1);

		jSplitPane2.setRightComponent(jTabbedPane1);

		jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		jSplitPane3.setResizeWeight(0.95);

		jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

		_edgeTypeGrp.add(_singlyBtn);
		_singlyBtn.setSelected(true);
		_singlyBtn.setText("Singly");
		_singlyBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				_singlyBtnActionPerformed(evt);
			}
		});

		_edgeTypeGrp.add(_doublyBtn);
		_doublyBtn.setText("Doubly");
		_doublyBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				_doublyBtnActionPerformed(evt);
			}
		});

		_edgeTypeGrp.add(_undirectedBtn);
		_undirectedBtn.setText("Undirected");
		_undirectedBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				_undirectedBtnActionPerformed(evt);
			}
		});

		jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel2.setText("Edges");
		jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addGap(48, 48, 48)
						.addComponent(_singlyBtn)
						.addGap(18, 18, 18)
						.addComponent(_doublyBtn)
						.addGap(18, 18, 18)
						.addComponent(_undirectedBtn)
						.addContainerGap(66, Short.MAX_VALUE))
		);
		jPanel1Layout.setVerticalGroup(
				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addComponent(jLabel2)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(_doublyBtn)
								.addComponent(_singlyBtn)
								.addComponent(_undirectedBtn))
								.addContainerGap(10, Short.MAX_VALUE))
		);

		jSplitPane3.setRightComponent(jPanel1);

		jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

		jTextArea1.setColumns(27);
		jTextArea1.setEditable(false);
		jTextArea1.setRows(5);
		jScrollPane2.setViewportView(jTextArea1);

		Font newTextFieldFont = new Font(jTextField1.getFont().getName(),Font.ITALIC,jTextField1.getFont().getSize());
		jTextField1.setText("Input string here");
		jTextField1.setFont(newTextFieldFont);
		jTextField1.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				jTextField1MousePressed(evt);
			}
		});
		jTextField1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jTextField1ActionPerformed(evt);
			}
		});

		_speedSlider.setMaximum(3000);
		_speedSlider.setValue(1500);
		_speedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				_sliderStateChanged(evt);
			}
		});

		_simSlide.setMaximum(MAX_SIM_SLIDER_VAL);
		_simSlide.setValue(0);
		_simSlide.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				_simSlideStateChanged(evt);
			}
		});

		_rewindBtn.setIcon(new javax.swing.ImageIcon(BWD_FILEPATH));
		_rewindBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				_rewindBtnActionPerformed(evt);
			}
		});

		_stopBtn.setIcon(new javax.swing.ImageIcon(STOP_FILEPATH));
		_stopBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				_stopBtnActionPerformed(evt);
			}
		});

		_playPauseBtn.setIcon(new javax.swing.ImageIcon(PLAY_FILEPATH));
		_playPauseBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				_playPauseBtnActionPerformed(evt);
			}
		});

		_forwardBtn.setIcon(new javax.swing.ImageIcon(FWD_FILEPATH));
		_forwardBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				_forwardBtnActionPerformed(evt);
			}
		});

		jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel1.setText("Simulation");
		jLabel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		
		JLabel simTextAreaLabel = new JLabel();
		simTextAreaLabel.setHorizontalAlignment(JLabel.CENTER);
		simTextAreaLabel.setFont(newTextFieldFont);
		simTextAreaLabel.setText("Simulation Console");
		
		JLabel minWait = new JLabel();
		minWait.setHorizontalAlignment(JLabel.CENTER);
		minWait.setFont(newTextFieldFont);
		minWait.setText("3s");
		
		JLabel maxWait = new JLabel();
		maxWait.setHorizontalAlignment(JLabel.CENTER);
		maxWait.setFont(newTextFieldFont);
		maxWait.setText("0s");
		
		JLabel simulationTimeSliderLabel = new JLabel();
		simulationTimeSliderLabel.setHorizontalAlignment(JLabel.CENTER);
		simulationTimeSliderLabel.setFont(newTextFieldFont);
		simulationTimeSliderLabel.setText("Interval Between Simulation Steps");
		
		JLabel simulationStepSlider = new JLabel();
		simulationStepSlider.setFont(newTextFieldFont);
		simulationStepSlider.setHorizontalAlignment(JLabel.CENTER);
		simulationStepSlider.setText("Simulation Slider: Scroll Through Simulation");

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(
				jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addGap(15, 15, 15)
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
								.addGroup(jPanel2Layout.createSequentialGroup()
										.addComponent(_rewindBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(_stopBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(_playPauseBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(_forwardBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addComponent(simulationTimeSliderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGroup(jPanel2Layout.createSequentialGroup()
												.addComponent(minWait, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(_speedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(maxWait, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
												.addComponent(simulationStepSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(_simSlide, javax.swing.GroupLayout.PREFERRED_SIZE,163,javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(simTextAreaLabel, javax.swing.GroupLayout.PREFERRED_SIZE,300,javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addGap(15, 15, 15))
		);
		jPanel2Layout.setVerticalGroup(
				jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addComponent(jLabel1)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(_rewindBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(_stopBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(_playPauseBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(_forwardBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(simulationTimeSliderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGroup(jPanel2Layout.createParallelGroup()
												.addComponent(minWait, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(_speedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(maxWait, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(simulationStepSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(_simSlide, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(simTextAreaLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
												.addContainerGap())
		);
		jSplitPane3.setLeftComponent(jPanel2);
		final BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) jSplitPane3.getUI();
		final BasicSplitPaneUI mainSplitPaneUI = (BasicSplitPaneUI) jSplitPane2.getUI();
		
		jPanel2.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {}
		});
		jPanel1.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {}
		});
		
		splitPaneUI.getDivider().addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent evt) {
				splitPaneUI.getDivider().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			public void mouseDragged(MouseEvent evt) {
				jSplitPane3.setDividerLocation(splitPaneUI.getDividerLocation(jSplitPane3));
			}
		});
		mainSplitPaneUI.getDivider().addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				mainSplitPaneUI.getDivider().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));	
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				jSplitPane2.setDividerLocation(mainSplitPaneUI.getDividerLocation(jSplitPane2));
			}
		});

		jSplitPane2.setLeftComponent(jSplitPane3);
		jSplitPane2.setOneTouchExpandable(true);

		jMenu3.setText("File");

		jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
		jMenuItem3.setText("New");
		jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem3ActionPerformed(evt);
			}
		});
		jMenu3.add(jMenuItem3);

		jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
		jMenuItem4.setText("Open");
		jMenu3.add(jMenuItem4);

		jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
		jMenuItem5.setText("Save");
		jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem5ActionPerformed(evt);
			}
		});
		jMenu3.add(jMenuItem5);

		jMenuItem7.setText("Save As...");
		jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem7ActionPerformed(evt);
			}
		});
		jMenu3.add(jMenuItem7);

		jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
		jMenuItem6.setText("Close Tab");
		jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem6ActionPerformed(evt);
			}
		});
		jMenu3.add(jMenuItem6);

		jMenuItem8.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
		jMenuItem8.setText("Exit");
		jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem8ActionPerformed(evt);
			}
		});
		jMenu3.add(jMenuItem8);

		jMenuBar2.add(jMenu3);

		jMenu4.setText("Edit");

		jMenuItemUndo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
		jMenuItemUndo.setText("Undo");
		jMenu4.add(jMenuItemUndo);

		jMenuItemRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
		jMenuItemRedo.setText("Redo");
		jMenu4.add(jMenuItemRedo);

		jMenuItemSelectAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
		jMenuItemSelectAll.setText("Select All");
		jMenuItemSelectAll.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				_nodesSelected = Collections.synchronizedSet(new HashSet<Node>());
				_edgesSelected = Collections.synchronizedSet(new HashSet<Edge>());
				Diagram diagram = drawingPanel1.getDiagram();
				for (Node n : diagram.getNodes()){
					n.setSelected(true);
					_nodesSelected.add(n);
				}
				for (Edge ed : diagram.getEdges()){
					ed.setSelected(true);
					_edgesSelected.add(ed);
				}
				drawingPanel1.repaint();
			}
			
		});
		jMenu4.add(jMenuItemSelectAll);
		
		
		
//		jMenuItemShowTrans.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
		jMenuItemShowTrans.setText("Show Transitions");
		jMenuTools.add(jMenuItemShowTrans);
		jMenuItemShowTrans.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				StringBuilder builder = new StringBuilder();
				for (Node node : drawingPanel1.getDiagram().getNodes()){
					for (Edge edge : node.getConnected()){
						if (edge.getStartNode() == node && edge.getDirection() == EdgeDirection.SINGLE){
							builder.append("<"+ node.getName() + ", edge labeled: " + edge.getTextField().getText() + " -> " + edge.getEndNode().getName() +  ">\n");
						}
					}
				}
				String disp = builder.toString();
				if (disp.equals("")) disp = "There are no transitions in this FSM.";
				String[] opts = {"OK"};

				JOptionPane.showOptionDialog(jSplitPane2, disp, "Transitions", JOptionPane.DEFAULT_OPTION, 
				JOptionPane.INFORMATION_MESSAGE, null, opts, opts[0]);
			}	
		});
		
		jMenuBar2.add(jMenu4);

		jMenuTools.setText("Tools");
		
		jMenuBar2.add(jMenuTools);

		jMenuHelp.setText("Help");
		jMenuItemAbout.setText("About");
		jMenuHelp.add(jMenuItemAbout);
		jMenuItemAbout.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame aboutFrame = new JFrame("About");
				aboutFrame.setVisible(true);
				JTabbedPane tabs = new JTabbedPane();
				tabs.setPreferredSize(new Dimension(400, 200));
				aboutFrame.add(tabs);
				JEditorPane edPane1 = new JEditorPane();
				edPane1.setEditable(false);
				edPane1.setContentType("text/html");
				edPane1.setText("<html> " +
						"<p><b>Note: many controls can be found immediately below the canvas while editing.</b></p><br>" +
						"<p><b>Key Controls</b><table> <tbody><tr><tr><td>Ctrl-A</td>" +
						"<td>Select all</tr></td></tr><tr> <td>Ctrl-S</td> <td>Save current tab</td> " +
						"</tr> <tr> <td>Ctrl-O</td> <td>Open tab from file</td> </tr><tr> <td>Ctrl-T</td> " +
						"<td>Open new blank tab</td>  </tr>  <tr>" +
						" <td>Ctrl-W</td> <td>Close current tab</td> </tr> <tr> <td>Ctrl-Q</td> " +
						"<td>Quit program</td>" +
						"<tr> <td>Ctrl-Z</td> <td>Undo</td>" +
						"<tr> <td>Ctrl-Y</td> <td>Redo</td> "  +
						"</tr></tbody></table></p>" + 
						
						"<p><b>Basic Mouse Controls (outside node/edge, no key modifiers)</b><table> <tbody><tr>" +
						"<tr> <td>Single-click</td> " +
						"<td> Deselect all</td>  </tr>" +
						"<tr> <td>Double-click</td> " +
						"<td> Create new node</td>  </tr>" +
						"<tr> <td>Drag</td> <td>Select within highlighted area</td>" +						
						"</tr></tbody></table></p>" +
						
						"<p><b>Basic Mouse Controls (on node, no key modifiers)</b><table> <tbody><tr>" +
						"<tr> <td>Single-click</td> " +
						"<td> Highlight this node only</td>  </tr>" +
						"<tr> <td>Double-click</td> " +
						"<td> Toggle node accept state</td>  </tr>" +
						"<tr> <td>Drag</td> <td>Move all selected nodes</td>" +						
						"</tr></tbody></table></p>" +
						
						"<p><b>Basic Mouse Controls (on edge, no key modifiers)</b><table> <tbody><tr>" +
						"<tr> <td>Single-click</td> " +
						"<td> Highlight this edge only</td>  </tr>" +
						"<tr> <td>Double-click</td> " +
						"<td> Create new node</td>  </tr>" +
						"<tr> <td>Drag</td> <td>Resize this edge only</td>" +						
						"</tr></tbody></table></p>" +
						
						
						"<p><b>Mouse Modifiers</b><table> <tbody><tr><tr> <td>Shift (hold)</td> " +
						"<td> Drag from a node to create new edge</td>  </tr>" +
						"<tr> <td>Ctrl (hold)</td> " +
						"<td> Click to select/deselect multiple nodes/edges</td>  </tr>" +
						"<tr> <td>S (press)</td> <td>Snap mouse to nearest node</td>" +						
						"</tr></tbody></table></p>"+
						
						"<p><b>Naming Nodes and Edges</b><table> <tbody><tr>" +
						"<tr> <td>_&#60;integer&#62;</td> " +
						"<td> Make &#60;integer&#62; subscript</td>  </tr>" +
						"<tr> <td>\\&#60;greek char name&#62;</td> " +
						"<td> Create greek character (alpha, beta, epsilon, theta only)</td>  </tr>" +
						"</tr></tbody></table></p>" );
				JScrollPane controls = new JScrollPane();
				controls.setViewportView(edPane1);
				edPane1.select(0, 0);
				controls.setName("Controls");
				edPane1.setVisible(true);
				edPane1.setSize(400,200);
				
				JEditorPane edPane2 = new JEditorPane();
				edPane2.setEditable(false);
				edPane2.setContentType("text/html");
				edPane2.setText("<html> <b>Developers</b><br> <br>Abhabongse (Plane) Janthong<br>Edward (Eddie) Grystar"+
						"<br>Elias (Eli) Wald <br>Sanford (Sandy) Student<br><br><b>For CS32 Spring 2012</b>");
				
				JScrollPane dev = new JScrollPane(edPane2);
				dev.setName("Development");
				tabs.add(dev);
				tabs.add(controls);
				aboutFrame.pack();
			}
			
		});
		
		
		jMenuBar2.add(jMenuHelp);

		setJMenuBar(jMenuBar2);

		_helpText.setText(help_message_text);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addComponent(_helpText, javax.swing.GroupLayout.DEFAULT_SIZE, 980, Short.MAX_VALUE)
				.addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1060, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
						.addGap(1)
						.addComponent(_helpText, javax.swing.GroupLayout.DEFAULT_SIZE, 10, 10)
						.addGap(2))
		);

		pack();
	}                   

	/**
	 * Called when a new tab is opened.  Creates a new drawing panel and pane associated with the new tab.
	 * Sets all appropriate listeners, and adds the new tab.
	 * @param evt	The ActionEvent associated with the tab menu item being clicked.
	 */
	private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {
		javax.swing.JScrollPane newPane = new javax.swing.JScrollPane();
		DrawingPanel newPanel = new DrawingPanel();
		newPane.setViewportView(newPanel);
		jScrollPane1 = newPane;
		drawingPanel1 = newPanel;
		drawingPanel1.addMouseListener(new DrawingPanelMouseListener(this));
		drawingPanel1.addMouseMotionListener(new DrawingPanelMouseMotionListener(this));
		drawingPanel1.addKeyListener(new DrawingPanelKeyListener(this));

		javax.swing.GroupLayout drawingPanel1Layout = new javax.swing.GroupLayout(drawingPanel1);
		drawingPanel1.setLayout(drawingPanel1Layout);
		drawingPanel1.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

		jTabbedPane1.addTab("Untitled", jScrollPane1);
		jTabbedPane1.setSelectedIndex(jTabbedPane1.getTabCount() - 1);
	}

	/**
	 * This is what happens when you click save.
	 * @param evt
	 */
	private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	/**
	 * This is what happens when you click save as.
	 * @param evt
	 */
	private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
	}

	/******************************************************************************************************************
	 * EXIT MENU, SIMULATION TEXT FIELD																				  *
	 ******************************************************************************************************************/
	
	/**
	 * Gets called when an action is performed on the simulation text
	 * field.  Nothing needs to happen; I think we can delete this.
	 */
	private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {
	}
	
	/**
	 * This gets called when the mouse is pressed inside the simulation
	 * text field.  Sets the text to be empty if the text is the
	 * default "Input string here".
	 */
	private void jTextField1MousePressed(java.awt.event.MouseEvent evt) {
		if (jTextField1.getText().equals("Input string here")){
			if (_simTimer.isRunning()) {
				_simTimer.stop();
			}
			jTextField1.setText("");
		}
		Font newTextFieldFont = new Font(jTextField1.getFont().getName(),Font.PLAIN,jTextField1.getFont().getSize());
		jTextField1.setFont(newTextFieldFont);
	}
	
	/**
	 * This gets called when the user hits ctrl-Q to quit the program.
	 * @param evt
	 */
	private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {
		exitPrompt();
		System.exit(0);
	}
	
	/**
	 * This gets called when the close tab action is performed.
	 * @param evt
	 */
	private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {
		//TODO: Add to this if-statement the condition that checks whether or
		//not the currently selected index has already been saved
		if (jTabbedPane1.getSelectedIndex() != -1) {
			int answer = exitPrompt();
			if (answer == 1) {
				//TODO: Save the tab before closing it!
			}
			if (answer != 2) {
				int currIndex = jTabbedPane1.getSelectedIndex();
				jTabbedPane1.remove(currIndex);
				if (jTabbedPane1.getSelectedComponent() != null){
					jScrollPane1 = (JScrollPane)(jTabbedPane1.getSelectedComponent());
					drawingPanel1 = (DrawingPanel)jScrollPane1.getViewport().getView();
				}
				else {
					jScrollPane1 = null;
					drawingPanel1 = null;
				}
			}
		}
		//If it has already been saved, then just copy above code to exit no matter what.
	}

	/******************************************************************************************************************
	 * SIMULATION METHODS: MOVE_FORWARD, MOVE_BACKWARD, STOP/PLAY/FWD/RWD CLICKED									  *
	 ******************************************************************************************************************/

	/**
	 * Helper method called when the simulation moves forward; use instead
	 * of calling "_fwdButton.doClick()".
	 */
	private void simulation_move_forward() {
		//If there are no tabs open, return.
		if (drawingPanel1 == null)
			return;
		
		//If simulation is not currently running, try starting it up.
		if (_sim == null) {
			//If the FSM is invalid, catch the error, display the message, and return.
			try {
				_sim = drawingPanel1.getDiagram().deterministicSimulation(jTextField1.getText());
			} catch (InvalidDFSMException ex) {
				_playPauseBtn.setIcon(new ImageIcon(PLAY_FILEPATH));
				jTextArea1.setText(ex.getMessage());
				if (_simTimer.isRunning()) {
					_simTimer.stop();
				}
				return;
			}
			//Else, start simulation, move the first step forward to the start node.
			jTextArea1.setText("");
			_iter = _sim.listIterator();
			_currentInputString = jTextField1.getText();
			if (!_autoChange) {
				_autoChange = true;
				_simSlide.setValue(0);
				_autoChange = false;
			}
		}

		//After potentially starting up the simulation (or if it's already been started)
		//clear the selected objects.
		drawingPanel1.clearCurrent();

		//If we still aren't at the end of our simulation, move to the next node.
		if (_iter.hasNext()) {
			//If the input text has changed, alert the user.
			int answer = 1;
			if (!_currentInputString.equals(jTextField1.getText())) {
				String[] opts = {"No", "Yes"};
				answer = JOptionPane.showOptionDialog(this, "The input string has changed during simulation.\nIf you continue," +
						" you may get unexpected results.  Do you wish to continue anyways?", "Input Text Changed", JOptionPane.YES_NO_OPTION, 
						JOptionPane.INFORMATION_MESSAGE, null, opts, opts[0]);
				_currentInputString = jTextField1.getText();
			}
			if (answer == 1) {
				//If backward was the last one to be clicked, make an extra call to
				//next() so that we move on to the correct next node.  Set the current
				//index of the input character in the input string to select.
				if (_backwardClicked) {
					_backwardClicked = false;
					_iter.next();
				}
				int curIndex = _iter.nextIndex();

				//Get the next object, set the slider value without stopping simulation.
				DiagramObject e = _iter.next();
				e.setCurrent(true);
				if (!_autoChange) {
					_autoChange = true;
					_simSlide.setValue(Math.min(MAX_SIM_SLIDER_VAL, _simSlide.getValue() + (int) Math.ceil(((double)MAX_SIM_SLIDER_VAL)/_sim.size())));
					_autoChange = false;
				}

				//Set the text area to be the appropriate text.
				if (jTextArea1.getText().equals("BACK TO START"))
					jTextArea1.setText("");
				if (jTextArea1.getText().equals(""))
					jTextArea1.setText("Start ");

				//Append the name of the next object in the simulation to the text area.
				jTextArea1.setText(jTextArea1.getText() + (e.getName() + "\n"));

				//Select the correct character in the input string, so the user knows which edge he's taking.
				jTextField1.grabFocus();
				if (curIndex < jTextField1.getText().length())
					jTextField1.select(curIndex, curIndex + 1);
				else
					jTextField1.select(0, 0);
				drawingPanel1.grabFocus();

				//If this is the last element in the list, then clean up and stop simulation.
				if (!_iter.hasNext()) {
					_playPauseBtn.setIcon(new ImageIcon(PLAY_FILEPATH));
					jTextArea1.setText(jTextArea1.getText() + ("FINISHED: Ended at " + e.getName() + ".\n"));
					if (((Node)e).isEnd())
						jTextArea1.setText(jTextArea1.getText() + ("FSM Accepted the input string.\n"));
					else
						jTextArea1.setText(jTextArea1.getText() + ("FSM Rejected the input string."));

					_sim = null;
					_iter = null;
					if (_simTimer.isRunning()) {
						_simTimer.stop();
					}
				}
			}
			else if (answer == 0) {
				_sim = null;
				_iter = null;
				_simTimer.stop();
				jTextArea1.setText("STOPPED");
				_playPauseBtn.setIcon(new ImageIcon(PLAY_FILEPATH));
				drawingPanel1.clearCurrent();
			}
		}
		//Repaint once at the end.
		drawingPanel1.repaint();
	}

	/**
	 * Helper method called when the simulation moves backward.  Use instead of calling
	 * "_rwdButton.doClick()".
	 */
	private void simulation_move_backward() {
		//If there are no tabs open, return.
		if (drawingPanel1 == null)
			return;
		
		//If the simulation is running, stop simulation.
		if (_simTimer.isRunning()) {
			_playPauseBtn.setIcon(new ImageIcon(PLAY_FILEPATH));
			_simTimer.stop();
		}
		//If we are not in the simulation, alert the user.
		if (_iter == null || _sim == null) {
			jTextArea1.setText("NOT IN SIMULATION");
			return;
		}

		//Clear all the currently selected (RED) nodes.
		drawingPanel1.clearCurrent();
		
		//If there is a previous node to go to
		if (_iter.hasPrevious()) {
			//If the input text has changed, alert the user.
			int answer = 1;
			if (!_currentInputString.equals(jTextField1.getText())) {
				String[] opts = {"No", "Yes"};
				answer = JOptionPane.showOptionDialog(this, "The input string has changed during simulation.\nIf you continue," +
						" you may get unexpected results.  Do you wish to continue anyways?", "Input Text Changed", JOptionPane.YES_NO_OPTION, 
						JOptionPane.INFORMATION_MESSAGE, null, opts, opts[0]);
				_currentInputString = jTextField1.getText();
			}
			if (answer == 1) {
				//If the last direction pressed was forwards, we need to do one extra
				//call to previous in order to actually move backwards instead of staying
				//at the same spot (which is how ListIterators work).  Set the index
				//of the input string to highlight.
				if (!_backwardClicked) {
					_backwardClicked = true;
					_iter.previous();
				}
				if (_iter.hasPrevious()) {
					int curIndex = _iter.nextIndex();

					//Get the previous node.  Set it to be current.
					DiagramObject e = _iter.previous();
					e.setCurrent(true);

					//Set the appropriate place of the second slider
					if (!_autoChange) {
						_autoChange = true;
						_simSlide.setValue(Math.min(MAX_SIM_SLIDER_VAL, _simSlide.getValue() - (int) Math.ceil(((double)MAX_SIM_SLIDER_VAL)/_sim.size())));
						_autoChange = false;
					}

					//Select the correct character in the input string, so the user knows which edge he's taking.
					jTextField1.grabFocus();
					if (curIndex - 1 >= 0)
						jTextField1.select(curIndex - 1, curIndex);
					drawingPanel1.grabFocus();

					//Take the last line of the text area off (remove the last node alert).
					String temp = jTextArea1.getText();
					jTextArea1.setText("");
					String[] tempArray = temp.split("\n");
					for (int i = 0; i < tempArray.length - 1; i ++) {
						jTextArea1.setText(jTextArea1.getText() + (tempArray[i] + "\n"));
					}
				}
				else
					backToStart();
			}
			else if (answer == 0) {
				_sim = null;
				_iter = null;
				_simTimer.stop();
				jTextArea1.setText("STOPPED");
				_playPauseBtn.setIcon(new ImageIcon(PLAY_FILEPATH));
				drawingPanel1.clearCurrent();
			}
		}

		//Otherwise, if there is on previous, we are back to the start.
		else
			backToStart();
		drawingPanel1.repaint();
	}
	
	/**
	 * Gets called when the "Stop" button is clicked.
	 * @param evt		The ActionEvent associated with the stop.
	 */
	private void _stopBtnActionPerformed(java.awt.event.ActionEvent evt) {
		//If there are no tabs open, return.
		if (drawingPanel1 == null)
			return;
		
		_sim = null;
		_iter = null;
		_simTimer.stop();
		jTextArea1.setText("STOPPED");
		_playPauseBtn.setIcon(new ImageIcon(PLAY_FILEPATH));
		drawingPanel1.clearCurrent();
		drawingPanel1.repaint();
	}
	
	/**
	 * Gets called when the "Play/Pause" button is clicked.
	 * @param evt		The ActionEvent associated with the play.
	 */
	private void _playPauseBtnActionPerformed(java.awt.event.ActionEvent evt) {
		//If there are no tabs open, return.
		if (drawingPanel1 == null)
			return;
		
		if (!_simTimer.isRunning()) {
			_playPauseBtn.setIcon(new ImageIcon(PAUSE_FILEPATH));
			simulation_move_forward();
			_simTimer.start();
			return;
		}
		else {
			_playPauseBtn.setIcon(new ImageIcon(PLAY_FILEPATH));
			_simTimer.stop();
		}
	}
	
	/**
	 * Gets called when the "Fwd" button is clicked.
	 * @param evt		The ActionEvent associated with the fwd.
	 */
	private void _forwardBtnActionPerformed(java.awt.event.ActionEvent evt) {
		simulation_move_forward();
	}
	
	/**
	 * Gets called when the "Bwd" button is clicked.
	 * @param evt		The ActionEvent associated with the bwd.
	 */
	private void _rewindBtnActionPerformed(java.awt.event.ActionEvent evt) {
		simulation_move_backward();
	}

	/******************************************************************************************************************
	 * SLIDERS STATE CHANGED, TOOLBOX BUTTONS CLICKED																  *
	 ******************************************************************************************************************/
	
	/**
	 * This gets called when the slider value changes that handles scrolling
	 * quickly through simulation.  Eddie made this method, and I can't follow
	 * the logic; he'll have to comment it.
	 */
	private void _simSlideStateChanged(javax.swing.event.ChangeEvent evt) {
		if (_sim == null) {
			return;
		}
		if (_autoChange) {
			return;
		}
		
		double percent = _simSlide.getValue()/(double)MAX_SIM_SLIDER_VAL;
		double cur_percent = _backwardClicked ? ((double)_iter.nextIndex())/_sim.size() : ((double)_iter.previousIndex())/_sim.size();
		
		double diff_percent = Math.abs(percent - cur_percent);
		
		int num_times_to_step = (int) (_sim.size()*diff_percent);
		if (num_times_to_step == 0)
			return;
		
		if (percent < cur_percent) {
			for (int i = 0; i < num_times_to_step; i ++) {
				_autoChange = true;
				simulation_move_backward();
				_autoChange = false;
			}
		}
		else if (percent > cur_percent) {
			for (int i = 0; i < num_times_to_step; i ++) {
				_autoChange = true;
				simulation_move_forward();
				_autoChange = false;
			}
		}
	}
	
	/**
	 * Gets called when the top slider that sets the time interval between
	 * simulation steps gets changed.
	 * @param evt
	 */
	private void _sliderStateChanged(javax.swing.event.ChangeEvent evt) {
		_simTimer.setDelay(_speedSlider.getMaximum() - _speedSlider.getValue());
	}
	
	/**
	 * This gets called when the Doubly sided edge button gets clicked.
	 * @param evt
	 */
	private void _doublyBtnActionPerformed(java.awt.event.ActionEvent evt) {
		//If there are no tabs open, return.
		if (drawingPanel1 == null)
			return;
		
		for (Edge e : _edgesSelected) {
			e.setDirection(EdgeDirection.DOUBLE);
		}
		_edgeType = EdgeDirection.DOUBLE;
		drawingPanel1.repaint();
	}
	
	/**
	 * This gets called when the Undirected edge button gets clicked.
	 * @param evt
	 */
	private void _undirectedBtnActionPerformed(java.awt.event.ActionEvent evt) {
		//If there are no tabs open, return.
		if (drawingPanel1 == null)
			return;
		
		for (Edge e : _edgesSelected) {
			e.setDirection(EdgeDirection.NONE);
		}
		_edgeType = EdgeDirection.NONE;
		drawingPanel1.repaint();
	}
	
	/**
	 * This gets called when the Singly directed edge button gets clicked.
	 * @param evt
	 */
	private void _singlyBtnActionPerformed(java.awt.event.ActionEvent evt) {
		//If there are no tabs open, return.
		if (drawingPanel1 == null)
			return;
		
		for (Edge e : _edgesSelected) {
			e.setDirection(EdgeDirection.SINGLE);
		}
		_edgeType = EdgeDirection.SINGLE;
		drawingPanel1.repaint();
	}


	/******************************************************************************************************************
	 * HELPER FUNCTIONS AND CLASSES																					  *
	 ******************************************************************************************************************/

	/**
	 * Listener used for the simulation timer, so that when the timer goes off,
	 * simulation can move forwards.
	 */
	private class SimListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			simulation_move_forward();
		}
	}
	
	/**
	 * Helper used in stepping forward/backward to alert user we are back to the start.
	 */
	public void backToStart() {
		jTextField1.select(0, 0);
		_playPauseBtn.setIcon(new ImageIcon(PLAY_FILEPATH));
		jTextArea1.setText("BACK TO START");
		_backwardClicked = false;
		//Set the appropriate place of the second slider
		if (!_autoChange) {
			_autoChange = true;
			_simSlide.setValue(Math.min(MAX_SIM_SLIDER_VAL, _simSlide.getValue() - (int) Math.ceil(((double)MAX_SIM_SLIDER_VAL)/_sim.size())));
			_autoChange = false;
		}
	}
	
	/**
	 * Returns 0 if the user does not want to save, but wants to close the window.
	 * Returns 1 if the user does want to save and close the window.
	 * Returns 2 if the user wants to cancel closing the window.
	 * @return		What the user wants to do.
	 */
	private int exitPrompt(){
		String[] opts = {"No", "Yes", "Cancel"};
		int answer = JOptionPane.showOptionDialog(this, "There is unsaved work! Would you like to save before closing" +
				" this tab?", "Closing Unsaved Tab", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opts, opts[1]);
		return answer;
	}
	
	/**
	 * This resets all the selected nodes/edges.
	 */
	public void resetSelected() {
		drawingPanel1.clearSelected();
		_nodesSelected = Collections.synchronizedSet(new HashSet<Node>());
		_edgesSelected = Collections.synchronizedSet(new HashSet<Edge>());
	}
	
	/**
	 * Getters for all the appropriate instance variables
	 */
	public DrawingPanel getDrawing() {
		return drawingPanel1;
	}
	
	public void addSelectedNode(Node n) {
		_nodesSelected.add(n);
		n.setSelected(true);
	}
	
	public void removeSelectedNode(Node n) {
		_nodesSelected.remove(n);
		n.setSelected(false);
	}
	
	public void addSelectedEdge(Edge e) {
		_edgesSelected.add(e);
		e.setSelected(true);
	}

	public void removeSelectedEdge(Edge e) {
		_edgesSelected.remove(e);
		e.setSelected(false);
	}

	public void setSelectedEdgeType(EdgeDirection dir) {
		switch (dir){
		case DOUBLE:
			_doublyBtn.setSelected(true);
			_edgeType = EdgeDirection.DOUBLE;
			break;
		case SINGLE:
			_singlyBtn.setSelected(true);
			_edgeType = EdgeDirection.SINGLE;
			break;
		default:
			_undirectedBtn.setSelected(true);
			_edgeType = EdgeDirection.NONE;

		}
	}
	
	public void deselectAllEdgeText() {
		if (_edgesSelected != null) {
			for (Edge e : _edgesSelected)
				e.getTextField().select(0, 0);
		}
	}
	
	/**
	 * Set the help text.  0 = general text, 1 = unselected node, 2 = selected node, 3 = edge.
	 * @param text
	 */
	public void setHelpText(int text) {
		switch (text) {
			case 0: _helpText.setText(help_message_text);
			case 1: _helpText.setText(help_message_text_in_node_unselected);
			case 2: _helpText.setText(help_message_text_in_node_selected);
			case 3: _helpText.setText(help_messate_text_in_edge);
			default: _helpText.setText(help_message_text);
		}
	}
	
	public void resetDrawingVariables() {
		_edgeDragged = null;
		_nodeDragged = null;
		_resizing = null;
		_selectPoint = null;
	}
	
	public void setEdgeStart(Node start) {
		_edgeStart = start;
	}
	
	public Node getEdgeStart() {
		return _edgeStart;
	}
	
	public void setNodeDragged(Node n) {
		_nodeDragged = n;
	}
	
	public Node getNodeDragged() {
		return _nodeDragged;
	}
	
	public void setResizing(Node n){
		_resizing = n;
	}
	
	public Node getResizing() {
		return _resizing;
	}
	
	public void setEdgeDragged(Edge e) {
		_edgeDragged = e;
	}	
	
	public Edge getEdgeDragged() {
		return _edgeDragged;
	}
	
	public void setSelectPoint(Point p) {
		_selectPoint = p;
	}
	
	public Point getSelectPoint() {
		return _selectPoint;
	}
	
	public EdgeDirection getEdgeType() {
		return _edgeType;
	}
	
	public void setMouseLoc(Point p) {
		_mouseLoc = p;
	}
	
	public Point getMouseLoc() {
		return _mouseLoc;
	}
	
	public Collection<Node> getNodesSelected() {
		return _nodesSelected;
	}
	
	public Collection<Edge> getEdgesSelected() {
		return _edgesSelected;
	}
	
	public Robot getRobot() {
		return _robot;
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		/*
		 * Create and display the form
		 */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new MainFrame().setVisible(true);
			}
		});
	}
		}

