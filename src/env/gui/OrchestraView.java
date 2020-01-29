package gui;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import javax.swing.*;


public class OrchestraView extends JFrame {
    private JPanel panel;
    private Random rand;
	public OrchestraView() {
		initComponents();
	}

	private void initComponents() {
		setTitle("Morchestra");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
        
        rand = new Random();

        panel = (JPanel)createContent();
        contentPane.add(panel);
        
		pack();
		setLocationRelativeTo(getOwner());
	}
    
    private Component createContent() {
        final Image image = requestImage();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
            }
        };

        panel.setLayout(null);

        panel.setPreferredSize(new Dimension(500, 405));

        return panel;
    }

    private Image requestImage() {
        Image image = null;

        try {
            image = ImageIO.read(new File("res/orchestra.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }


    public void addAgent(String id, String type)
    {
        JPanel agent = new JPanel();
        agent.setBackground(getColor(type));
        agent.setPreferredSize(new Dimension(20, 20));
        agent.setLayout(null);
        panel.add(agent);
        
        Insets insets = panel.getInsets();
        Dimension psize = panel.getPreferredSize();

        setAgentBounds(agent, type, insets.left, insets.top, psize.width, psize.height);

        panel.revalidate();
    }

    public void removeAgent(String id)
    {

    }

    public void setMusic(String name)
    {
        setTitle("Morchestra - "+name);
    }

    private Color getColor(String type)
    {
        if(type.equals("conductor"))
            return Color.BLACK;
        else
            return Color.BLUE;
    }

    private void setAgentBounds(JPanel agent, String type, int left, int top, int width, int height)
    {
        int x = 0, y = 0;
        int w = 20;
        int h = 20;
        if(type.equals("conductor"))
        {
            x = (width - w) / 2 - 10;
            y = height - 2 * h - 10;
        }
        else
        {
            x = rand.nextInt(width);
            y = rand.nextInt(height);
        }
        agent.setBounds(x + left, y + top, w, h);
    }
}