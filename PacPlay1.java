import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

class Position
{
	//Attributes
	int x;
	int y;
	
	//Constructor
	public Position()
	{
		x = y = 0;
	}
	
	public Position(int a, int b)
	{
		x = a;
		y = b;
	}
	
	//Methods
	public void setX(int n)
	{
		x = n;
	}
	
	public void setY(int n)
	{
		y = n;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
}

//class used to make the maze for the game to be played in
class Node extends Position
{
	//Attributes
	int fruit; // 1 if there is a fruit there or 0 if its not
	Node next;
	
	//Constructor
	public Node()
	{
		super();
		fruit = 1;
		next = null;
	}
	
	public Node(int a, int b)
	{
		super(a,b);
		fruit = 1;
		next = null;
	}
	
	//Methods
	public void setNext(Node n)
	{
		next = n;
	}
	
	public void setFruit(int n)
	{
		fruit = n;
	}
	
	public Node getNext()
	{
		return next;
	}
	
	public int getFruit()
	{
		return fruit;
	}
}

class Pacman extends Position
{
	//Attributes
	int dir;
	/*
	use a unique number to signify each direction
	1 - left
	2 - down
	3 - right
	4 - up */
	
	//Constructor
	public Pacman(int a, int b)
	{
		super(a,b);
		dir = 1;
	}
	
	//Methods
	public int getDir()
	{
		return dir;
	}
	
	public void setDir(int n)
	{
		dir = n;
	}
}

class PacGame extends JFrame implements KeyListener
{
	char x;
	boolean ret; //true while game is still going on.
	private DrawStuff maze;
	private static final int dim=30;
	private static final int height=(11+2)*dim;
	private static final int width=(11+2)*dim;
	private static final int stx=60; //start x
	private static final int sty=60; //start y
	private	static final int winwidth  = 640; //window width
	private static final int winht = 480; //window height
	Node[] stage;
	int size;
	static int fc; //contains the count of the total fruits
	Pacman pac;//new
	Position g1, g2, g3, g4;
	boolean startscreen;
	boolean endscreen;
	boolean instruct;
	
	//returns the corresponding x coordinates in the maze from the index of the array
	public int compX(int n)
	{
		int x = n % 11;
		if(x == 0)
			x = 11;
		return x;
	}
	
	//returns the corresponding y coordinates in the maze from the index of the array
	public int compY(int n)
	{
		int x = (n / 11) + 1;
		if(x == 12)
			--x;
		return x;
	}
	
	//returns the corresponding index of the element in the array given a coordinate in the maze
	public int compI(int x, int y)
	{
		int i = 0;
		i += (y - 1) * 11;
		i += x;
		return i;
	}
	
	//this function creates all the links between 2 vertices
	private void link(int y, int x, int y1 , int x1)
	{
		int i = compI(x,y);
		Node temp = new Node(x1,y1);
		if(stage[i].getNext() != null)
		{
			temp.setNext(stage[i].getNext());
		}
		stage[i].setNext(temp);
		int j = compI(x1,y1);
		Node temp1 = new Node(x,y);
		if(stage[j].getNext() != null)
		{
			temp1.setNext(stage[j].getNext());
		}
		stage[j].setNext(temp1);
	}
	
	//function searches if the edge with coordinates being passed to it can be gone to from the element of the array i(represents a coordinate in the maze)
	//basically if there is an edge between the two elements of the maze
	public boolean search(int x, int y, int i)
	{
		Node t = stage[i].getNext();
		boolean res = false;
		while(t != null)
		{
			if(t.getX() == x && t.getY() == y)
				res = true;
			t = t.getNext();
		}
		return res;
	}
	
	//this is the function that moves the pacman depending on the direction he is moving if it is possible
	public void move()
	{
		int d = pac.getDir();
		int x = pac.getX();
		int y = pac.getY();
		int i = compI(x,y);
		switch(d)
		{
			case 1:
			--x;
			break;
			case 2:
			++y;
			break;
			case 3:
			++x;
			break;
			case 4:
			--y;
			break;
		}
		
		//this is to check for the going through walls at the centers of each outer edges
        if(y == 12 && x == 6)
			y = 1;
		else if(y == 0 && x == 6)
			y = 11;
		else if(x == 0 && y == 6)
			x = 11;
		else if(x == 12 && y == 6)
			x = 1;
		
		//if the edge exists update the position of pacman and move him there and draw the updated positions of everything
		if(search(x,y,i))
		{
			//move it n update the fruit in stage[i]
			if(stage[i].getFruit() != 0)
			{
				stage[i].setFruit(0);
				--fc;
			}
			pac.setX(x);
			pac.setY(y);
		}
      	maze=new DrawStuff();
		maze.setPreferredSize(new Dimension(winwidth,winht));
		add(maze);
		pack();

	}
	
	//checks if pacman has moved to the same position as a ghost
	public boolean check(Node t)
	{
		boolean ret = false;
		int x = t.getX();
		int y = t.getY();
		if(x == g1.getX() && y == g1.getY())
			ret = true;
		else if(x == g2.getX() && y == g2.getY())
			ret = true;
		else if(x == g3.getX() && y == g3.getY())
			ret = true;
		else if(x == g4.getX() && y == g4.getY())
			ret = true;
      return ret;
	}
	
	//moves the ghosts in a random manner along the edges between the various vertices
	public void gmove()
	{
		Random rand = new Random();
		
		//ghost 1
		int x = g1.getX();
		int y = g1.getY();
		int i = compI(x, y);
		Node t = stage[i];
		int r = rand.nextInt(4) + 1;
		for(int j = 0; j < r; ++j)
		{
			if(t.getNext() == null)
				break;
			else
				t = t.getNext();   
		}
		g1.setX(t.getX());
		g1.setY(t.getY());
		
		//ghost 2
		r = rand.nextInt(4) + 1;
		x = g2.getX();
		y = g2.getY();
		i = compI(x, y);
		t = stage[i];
		for(int j = 0; j < r; ++j)
        {
			if(t.getNext() == null)
				break;
			else
				t = t.getNext();   
        }
		g2.setX(t.getX());
		g2.setY(t.getY());
		
		//ghost 3
		r = rand.nextInt(4) + 1;
		x = g3.getX();
		y = g3.getY();
		i = compI(x, y);
		t = stage[i];
		for(int j = 0; j < r; ++j)
        {
			if(t.getNext() == null)
				break;
			else
				t = t.getNext();   
        }
		g3.setX(t.getX());
		g3.setY(t.getY());
		
		//ghost 4
		r = rand.nextInt(4) + 1;
		x = g4.getX();
		y = g4.getY();
		i = compI(x, y);	
		t = stage[i];
		for(int j = 0; j < r; ++j)
        {
			if(t.getNext() == null)
				break;
			else
				t = t.getNext();   
        }
		g4.setX(t.getX());
		g4.setY(t.getY());
	}
	
	//the main function where the game is played till either the player wins or loses
	public void zoomyay()
	{ 
		maze=new DrawStuff();
		maze.setPreferredSize(new Dimension(winwidth,winht));
		this.add(maze);
		pack();
		while (ret)
		{
			System.out.print("");
			if (startscreen==false && instruct==false)
			{
				for (double i=0;i<100000000;i++);
				for (double i=0;i<100000000;i++);
				if (fc==0)
					ret=false;
				move();
				int i = compI(pac.getX(), pac.getY());
				if(check(stage[i]))
				{
					endscreen=true;
					ret = false;
					break;
				}
		 		gmove();
	 			if(check(stage[i]))
				{
					ret = false;
					endscreen=true;
					break;
				}
			}
		}
		
		//checks for gameover condition
		if (!ret && fc!=0)
		{
			System.out.print("");
			maze=new DrawStuff();
			maze.setPreferredSize(new Dimension(winwidth,winht));
			this.add(maze);
			pack();
		}
	}
	
	
	//Constructor
	public PacGame()
	{
		startscreen=true;
		instruct=false;
		endscreen=false;
		int j;
		stage = new Node[122];
		size = 122;
		fc = size - 18;
		for(int i = 1; i < 122; ++i)
			stage[i] = new Node(compX(i), compY(i));
		
		//Initial position of pacman
		pac = new Pacman(6,8);
		g1 = new Position(6,6);
		g2 = new Position(5,7);
		g3 = new Position(6,7);
		g4 = new Position(7,7);
		
		//Remving the extra fruits
		j = compI(6,6);
		stage[j].setFruit(0);
		j = compI(5,6);
		stage[j].setFruit(0);
		j = compI(6,7);
		stage[j].setFruit(0);
		j = compI(5,7);
		stage[j].setFruit(0);
		j = compI(7,6);
		stage[j].setFruit(0);
		j = compI(7,7);
		stage[j].setFruit(0);
		j = compI(4,3);
		stage[j].setFruit(0);
		j = compI(3,3);
		stage[j].setFruit(0);
		j = compI(9,3);
		stage[j].setFruit(0);
		j = compI(10,3);
		stage[j].setFruit(0);
		j = compI(9,10);
		stage[j].setFruit(0);
		j = compI(10,10);
		stage[j].setFruit(0);
		j = compI(3,10);
		stage[j].setFruit(0);
		j = compI(4,10);
		stage[j].setFruit(0);
		j = compI(2,6);
		stage[j].setFruit(0);
		j = compI(9,6);
		stage[j].setFruit(0);
		
		//Creating the edges betweeen the vertices
		link(1,1,1,2);
		link(1,2,1,3);
		link(1,3,1,4);
		link(1,4,1,5);
		link(1,5,1,6);
		link(1,6,1,7);
		link(1,7,1,8);
		link(1,8,1,9);
		link(1,9,1,10);
		link(1,10,1,11);
		link(2,2,2,3);
		link(2,3,2,4);
		link(2,4,2,5);
		link(2,7,2,8);
		link(2,8,2,9);
		link(2,9,2,10);
		link(2,10,2,11);
		link(4,2,4,3);
		link(4,3,4,4);
		link(4,4,4,5);
		link(4,5,4,6);
		link(4,7,4,8);
		link(4,8,4,9);
		link(4,9,4,10);
		link(4,10,4,11);
		link(5,1,5,2);
		link(5,2,5,3);
		link(5,3,5,4);
		link(5,4,5,5);
		link(5,5,5,6);
		link(5,6,5,7);
		link(5,7,5,8);
		link(5,8,5,9);
		link(5,9,5,10);
		link(6,3,6,4);
		link(6,5,6,6);
		link(6,6,6,7);
		link(6,11,6,1);
		link(7,1,7,2);
		link(7,3,7,4);
		link(7,5,7,6);
		link(7,6,7,7);
		link(7,8,7,9);
		link(7,9,7,10);
		link(8,2,8,3);
		link(8,3,8,4);
		link(8,4,8,5);
		link(8,5,8,6);
		link(8,6,8,7);
		link(8,7,8,8);
		link(8,8,8,9);
		link(8,9,8,10);
		link(8,10,8,11);
		link(9,2,9,3);
		link(9,3,9,4);
		link(9,4,9,5);
		link(9,6,9,7);
		link(9,8,9,9);
		link(9,9,9,10);
		link(9,10,9,11);
		link(10,5,10,6);
		link(10,6,10,7);
		link(11,1,11,2);
		link(11,2,11,3);
		link(11,3,11,4);
		link(11,4,11,5);
		link(11,5,11,6);
		link(11,6,11,7);
		link(11,7,11,8);
		link(11,8,11,9);
		link(11,9,11,10);
		link(11,10,11,11);
		link(1,1,2,1);
		link(2,1,3,1);
		link(3,1,4,1);
		link(4,1,5,1);
		link(5,1,6,1);
		link(6,1,7,1);
		link(7,1,8,1);
		link(8,1,9,1);
		link(9,1,10,1);
		link(10,1,11,1);
		link(2,2,3,2);
		link(3,2,4,2);
		link(4,2,5,2);
		link(7,2,8,2);
		link(9,2,10,2);
		link(10,2,11,2);
		link(6,3,7,3);
		link(2,3,1,3);
		link(4,4,5,4);
		link(5,4,6,4);
		link(6,4,7,4);
		link(7,4,8,4);
		link(2,5,3,5);
		link(3,5,4,5);
		link(6,5,7,5);
		link(9,5,10,5);
		link(10,5,11,5);
		link(2,6,3,6);
		link(2,6,1,6);
		link(3,6,4,6);
		link(5,6,6,6);
		link(6,6,7,6);
		link(8,6,9,6);
		link(11,6,1,6);
		link(1,7,2,7);
		link(2,7,3,7);
		link(3,7,4,7);
		link(6,7,7,7);
		link(9,7,10,7);
		link(1,8,2,8);		
		link(2,8,3,8);
		link(3,8,4,8);
		link(4,8,5,8);
		link(5,8,6,8);
		link(6,8,7,8);
		link(7,8,8,8);
		link(9,8,10,8);
		link(10,8,11,8);
		link(5,10,6,10);
		link(6,10,7,10);
		link(8,10,9,10);
		link(1,11,2,11);
		link(2,11,3,11);
		link(3,11,4,11);
		link(4,11,5,11);
		link(5,11,6,11);
		link(6,11,7,11);
		link(7,11,8,11);
		link(9,11,10,11);	
		link(10,11,11,11); 
		
		setDefaultCloseOperation(EXIT_ON_CLOSE); 
		pack();
		setVisible(true);
		setTitle("PacMan");
		this.addKeyListener(this);
		x='a';
		ret=true;
	}
	
	//class for the graphics													S
	class DrawStuff extends JPanel
	{
		private BufferedImage image1;
		@Override
		public void paintComponent(Graphics x)
		{
			if (startscreen==true)
			{
				super.paintComponent(x);
				setBackground(Color.BLACK);
				setOpaque(true);

				try
				{
				    image1 = ImageIO.read(new URL("file:///Users/Nidhi/Desktop/DSProj/pac-man.png"));//change as necessary
				}
				catch(IOException ioe)
				{
				    System.out.println("Unable to fetch image.");
				    ioe.printStackTrace();
				}
				x.drawImage(image1, 0, 0,getWidth(), getHeight() - 1, this);
				setVisible(true);
			}
			else if (instruct==true)
			{
				super.paintComponent(x);
				setBackground(Color.BLACK);
				setOpaque(true);
				try
				{
				    image1 = ImageIO.read(new URL("file:///Users/Nidhi/Desktop/DSProj/instruct.png"));//change as necessary
				}
				catch(IOException ioe)
				{
				    System.out.println("Unable to fetch image.");
				    ioe.printStackTrace();
				}
				x.drawImage(image1, 0, 0,getWidth(), getHeight() - 1, this);
				setVisible(true);
			}
			else if (endscreen==true)
			{
				super.paintComponent(x);
				setBackground(Color.BLACK);
				setOpaque(true);
				try
				{
				    image1 = ImageIO.read(new URL("file:///Users/Nidhi/Desktop/DSProj/pac1.png"));//change as necessary
				}
				catch(IOException ioe)
				{
				    System.out.println("Unable to fetch image.");
				    ioe.printStackTrace();
				}
				x.drawImage(image1, 0, 0,getWidth(), getHeight() - 4, this);
				setVisible(true);
			}
			else if (fc==0)
			{
				super.paintComponent(x);
				setBackground(Color.BLACK);
				setOpaque(true);
				try
				{
				    image1 = ImageIO.read(new URL("file:///Users/Nidhi/Desktop/DSProj/youwin.png"));//change as necessary   
				}
				catch(IOException ioe)
				{
				    System.out.println("Unable to fetch image.");
				    ioe.printStackTrace();
				}
				x.drawImage(image1, 0, 0,getWidth(), getHeight() - 4, this);
				setVisible(true);
			}
			else
			{
				int tx=stx;
				int ty=sty;
				super.paintComponent(x);
				setBackground(Color.BLACK);
				Graphics2D g = (Graphics2D)x.create();
				g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.setColor(Color.CYAN);
				int i,j;
				/*
				x.setColor(Color.GRAY);
				for (i=stx;i<width;i+=dim) 				  //Grid
				{
					for (j=sty;j<height;j+=dim)
					{
						x.drawLine(i,j,i+dim,j);
						x.drawLine(i,j,i,j+dim);
						x.drawLine(i+dim,j,i+dim,j+dim);
					}
				} 
				*/
				g.drawLine(stx,sty,stx+dim*5,sty); //top edge
				g.drawLine(stx+dim*6,sty,width,sty); //top edge
				g.drawLine(stx,sty,stx,sty+dim*5); //left edge
				g.drawLine(stx,sty+dim*6,stx,height); //left edge
				g.drawLine(stx,height,stx+dim*5,height); //bottom edge
				g.drawLine(stx+dim*6,height,width,height);//bottom edge
				g.drawLine(width,sty,width,sty+dim*5); //right edge
				g.drawLine(width,sty+dim*6,width,height); //right edge
				//Maaaaaaaaaaaze:
				tx=stx+dim;
				ty=sty+dim;
				g.drawLine(tx,ty,tx+dim,ty); //line1
				tx+=dim*2;
				g.drawLine(tx,ty,tx+dim*2,ty); //line2
				tx+=dim*3;
				//g.drawLine(tx,ty,ty+dim,ty);//line3
				tx+=dim*2;
				g.drawLine(tx,ty,tx+dim*2,ty);//line4
				tx=stx+dim;
				g.drawLine(tx,ty,tx,ty+dim*3);//line5
				tx+=dim;
				ty+=dim;
				g.drawLine(tx,ty,tx+dim*2,ty);//line6
				g.drawLine(tx,ty,tx,ty+dim);//line7
				tx+=dim*2;
				ty+=dim;
				g.drawLine(tx,ty-dim,tx,ty);//line8
				g.drawLine(tx-dim*2,ty,tx,ty);//line9
				tx+=dim;
				ty-=dim*2;
				g.drawLine(tx,ty,tx,ty+dim*2);//line10
				tx+=dim;
				g.drawLine(tx,ty,tx,ty+dim*3);//line11
				ty+=dim;
				tx+=dim;
				g.drawLine(tx,ty,tx,ty+dim);//line12
				tx+=dim;
				g.drawLine(tx,ty,tx+dim*2,ty);//box2
				g.drawLine(tx,ty,tx,ty+dim);//box2
				tx+=dim*2;
				ty+=dim;
				g.drawLine(tx,ty-dim,tx,ty);//box2
				g.drawLine(tx-dim*2,ty,tx,ty);//box2
				ty+=dim;
				g.drawLine(tx,ty,tx,ty+dim*3);//line13
				g.drawLine(tx,ty,tx-dim*2,ty);//line14
				g.drawLine(tx,ty+dim*3,tx-dim*2,ty+dim*3);//line15
				tx-=dim*2;
				ty+=dim;
				g.drawLine(tx,ty,tx+dim,ty);//box3
				g.drawLine(tx,ty,tx,ty+dim);//box3
				tx+=dim;
				ty+=dim;
				g.drawLine(tx,ty-dim,tx,ty);//box3
				g.drawLine(tx-dim,ty,tx,ty);//box3
				ty-=dim;
				tx-=dim*5;
				g.drawLine(tx,ty,tx+dim,ty);//box4
				g.drawLine(tx+dim*2,ty,tx+dim*3,ty);//box4
				g.drawLine(tx,ty,tx,ty+dim*2);//box4
				tx+=dim*3;
				ty+=dim*2;
				g.drawLine(tx,ty-dim*2,tx,ty);//box4
				g.drawLine(tx-dim*3,ty,tx,ty);//box4
				tx-=dim*3;
				ty-=dim*3;
				g.drawLine(tx,ty,tx+dim*3,ty);//line16
				tx-=dim*2;
				g.drawLine(tx,ty,tx+dim,ty);//line17
				tx-=dim;
				ty+=dim;
				g.drawLine(tx,ty,tx+dim,ty);//box5
				g.drawLine(tx,ty,tx,ty+dim);//box5
				tx+=dim;
				g.drawLine(tx,ty,tx+dim,ty);//line18
				ty+=dim;
				g.drawLine(tx,ty-dim,tx,ty);//box5
				g.drawLine(tx-dim,ty,tx,ty);//box5
				g.drawLine(tx,ty,tx,ty+dim);//line19
				g.drawLine(tx,ty+dim,tx+dim,ty+dim);//line20
				tx-=dim;
				ty+=dim;
				g.drawLine(tx,ty,tx,ty+dim*3);//line21
				g.drawLine(tx,ty+dim,tx+dim*4,ty+dim);//line22
				tx+=dim;
				ty+=dim*2;
				g.drawLine(tx,ty,tx+dim*2,ty);//box6
				g.drawLine(tx,ty,tx,ty+dim);//box6
				tx+=dim*2;
				ty+=dim;
				g.drawLine(tx,ty-dim,tx,ty);//box6
				g.drawLine(tx-dim*2,ty,tx,ty);//box6
				tx+=dim;
				ty-=dim*2;
				g.drawLine(tx,ty,tx,ty+dim);//line23
				g.drawLine(tx,ty+dim,tx+dim,ty+dim);//line24
				//////////
				tx+=dim;
				g.drawLine(tx,ty,tx+dim*3,ty);//line25
				g.drawLine(tx+dim,ty,tx+dim,ty+dim*2);//line26
				g.drawLine(tx-dim,ty+dim*2,tx+dim,ty+dim*2);//line27
				g.drawLine(tx+dim*4,ty,tx+dim*5,ty);//line28
				tx+=dim*2;
				ty+=dim;
				g.drawLine(tx,ty,tx+dim*2,ty);//box7
				g.drawLine(tx,ty,tx,ty+dim);//box7
				tx+=dim*2;
				ty+=dim;
				g.drawLine(tx,ty-dim,tx,ty);//box7
				g.drawLine(tx-dim*2,ty,tx,ty);//box7
				
				//draws the fruits
				for (tx=1;tx<=11;tx++)
				{
					for (ty=1;ty<=11;ty++)
					{
						i=compI(tx,ty);
						if (stage[i].getFruit() > 0)
						{
							x.setColor(Color.MAGENTA);
							x.fillOval(stx+dim*(tx-1)+(dim/3),stx+dim*(ty-1)+(dim/2),4,4);
						}
					}
				}
				//draws the pacman depending on its position
				tx=pac.getX();
				ty=pac.getY();
				x.setColor(Color.YELLOW);
				x.fillOval(stx+dim*(tx-1)+(dim/4),stx+dim*(ty-1)+(dim/4),17,17);
			
				//draws the ghosts depending on thier positions
				tx = g1.getX();
				ty = g1.getY();
				x.setColor(Color.RED);
				x.fillOval(stx+dim*(tx-1)+(dim/4),stx+dim*(ty-1)+(dim/4),17,17);
			
				tx = g2.getX();
				ty = g2.getY();
				x.setColor(Color.BLUE);
				x.fillOval(stx+dim*(tx-1)+(dim/4),stx+dim*(ty-1)+(dim/4),17,17);
			
				tx = g3.getX();
				ty = g3.getY();
				x.setColor(Color.PINK);
				x.fillOval(stx+dim*(tx-1)+(dim/4),stx+dim*(ty-1)+(dim/4),17,17);
			
				tx = g4.getX();
				ty = g4.getY();
				x.setColor(Color.CYAN);
				x.fillOval(stx+dim*(tx-1)+(dim/4),stx+dim*(ty-1)+(dim/4),17,17);
			}
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}
 
	@Override
	public void keyPressed(KeyEvent e) 
	{
		if (startscreen==true)
		{
			startscreen=false;
			instruct=true;
			maze=new DrawStuff();
			maze.setPreferredSize(new Dimension(winwidth,winht));
			this.add(maze);
			pack();
		}
		else if (instruct==true)
		{	
			instruct=false;
			maze=new DrawStuff();
			maze.setPreferredSize(new Dimension(winwidth,winht));
			this.add(maze);
			pack();
		}
		else
		{	
			if (e.getKeyChar()=='a')
				pac.setDir(1);
			else if (e.getKeyChar()=='w')
				pac.setDir(4);
			else if (e.getKeyChar()=='d')
				pac.setDir(3);
			else if (e.getKeyChar()=='s')
				pac.setDir(2);
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {}
}

//main function
class PacPlay
{
	public static void main(String[] args)
	{
		PacGame game=new PacGame();
		game.zoomyay();
	}
}
