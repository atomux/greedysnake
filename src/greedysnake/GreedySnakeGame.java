package greedysnake;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GreedySnakeGame extends JFrame implements Runnable, ActionListener {

    final String flag = "Flag1";
    // 参数
    final int WAIT_TIME = 460;
    final int WIDTH = 300;
    final int HEIGHT = 300;
    final String TITLE = "贪吃蛇";

    GreedySnake snake;
    Canvas canvas;
    Thread thread;
    JMenuItem item_startOrRestart, item_quit;
    JMenu menu_score;
    
    int score;
    boolean running = true;

    GreedySnakeGame() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu menu_game, menu_other;
        menu_game = new JMenu("游戏");
        menu_score = new JMenu("");
        menu_score.setEnabled(false);
        menuBar.add(menu_game);
        menuBar.add(menu_score);
        item_startOrRestart = new JMenuItem("开始");
        item_quit = new JMenuItem("退出");
        item_startOrRestart.addActionListener(this);
        item_quit.addActionListener(this);
        menu_game.add(item_startOrRestart);
        menu_game.add(item_quit);

        this.setVisible(true);
        // 为了准确的画出的框架的尺寸，需要使用 Insets类
        // 要先设置框架可见 java.lang.Conponent.Insets的属性才会被确定
        Insets insets = this.getInsets();
        this.setBounds(100, 100, WIDTH + insets.left
                + insets.right, HEIGHT + insets.top + insets.bottom);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle(TITLE);

        canvas = new MyCanvas();
        this.add(canvas);
        validate();
    }

    @Override
    public void run() {
        try {
            do {
                canvas.repaint();
                score = snake.getScore();
                menu_score.setText("当前分数: " + score);
                synchronized (flag) {
                    flag.wait(WAIT_TIME);
                }
            } while (snake.crawl() && running);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (running == true) {
                JOptionPane.showMessageDialog(null, "游戏结束");
            }
            score = 0;
            item_startOrRestart.setText("开始");
        }
    }

    class MyKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            // 碰撞判断, 蛇不可能向着自己移动方向的反向移动
            if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) {
                snake.setDir(GreedySnake.DIR_UP);
            } else if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN) {
                snake.setDir(GreedySnake.DIR_DOWN);
            } else if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) {
                snake.setDir(GreedySnake.DIR_LEFT);
            } else if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                snake.setDir(GreedySnake.DIR_RIGHT);
            }
            if (null != thread) {
                synchronized (flag) {
                    flag.notify();
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == item_startOrRestart) {
            // 第一次点击
            if (thread == null) {
                item_startOrRestart.setText("刷新");
                // 游戏中途点击
            } else if (thread.isAlive()) {
                score = 0;
                running = false;
                // 空语句, 直到thread线程完成
                while (thread.isAlive())  ;  // 此处空语句
                running = true;
                snake = null;
                thread = null;
            }
            menu_score.setText("当前分数: " + score);
            try {
                snake = new GreedySnake(canvas);
                canvas.addKeyListener(new MyKeyListener());
                canvas.requestFocus();
                thread = new Thread(this);
                thread.start();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        } else if (e.getSource() == item_quit) {
            System.exit(0);
        }
    }

    class MyCanvas extends Canvas {
        // 设置双重缓冲, 防止画面撕裂
        private Image iBuffer;
        private Graphics gBuffer;
        @Override
        public void update(Graphics g){
            paint(g);
        }
        
        @Override
        public void paint(Graphics g) {
            if(iBuffer == null){
                iBuffer = createImage(this.getSize().width, this.getSize().height);
                gBuffer = iBuffer.getGraphics();
            }
            gBuffer.setColor(getBackground());
            gBuffer.fillRect(0,0,this.getSize().width,this.getSize().height);
            if (snake != null) {
                snake.drawAll(gBuffer);
            }
            gBuffer.setColor(Color.BLACK);
            gBuffer.drawRect(0, 0, canvas.getSize().width - 1, canvas.getSize().height - 1);
            g.drawImage(iBuffer,0,0,this);
        }
    }

    public static void main(String[] args) {
        new GreedySnakeGame();
    }
}
