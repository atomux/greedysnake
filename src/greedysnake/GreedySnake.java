package greedysnake;


import java.awt.*;
import java.util.*;

/*
 *   游戏主体模块
 *   使用: 1.实例化类
 *         2.新建一个线程控制活动( 方法 crawl() ), 通过返回值判断是否继续
 *         3.用一个键盘监听开关控制方向( 方法 setDir(int Dir) ).
 *         4.在画布中调用 drawAll();
*/

public class GreedySnake{
    // 常量
    public final static int DIR_LEFT = 0x01;
    public final static int DIR_RIGHT = 0x02;
    public final static int DIR_UP = 0x03;
    public final static int DIR_DOWN = 0x04;
    // list_body 代表蛇身
    // 行为有 "移动" 和 "吃食物"
    private ArrayList<Point> list_body;
    // 游戏地图, 提供长宽信息
    private final Canvas game_map;
    // 目前头的方向, head_dir开放一个API接口
    private int head_dir;
    // 空隙大小, 决定地图相对大小
    private int SNAKE_LENGTH = 3;
    private int PADDING = 20;
    private int GENERATE_RATE = 16;
    private final int MAP_HEIGHT;
    private final int MAP_WIDTH;
    // 地图每个点组成的数组, 用于判断是安全的生产 "食物" 小点
    private final boolean[][] isOccupied;
    // 移动次数的一个计数器, 用于"食物生成速率"
    private int counter = GENERATE_RATE; 
    // 提供一个高效的存放和查找"食物"位置容器
    private HashMap<Integer,Point> map_food;
    private ArrayList<Integer> list_key;
    // 简单分数计数
    private int score;
    
    public GreedySnake(Canvas game_map) throws Exception{
        // 获得game_map长宽,做地图的自适应
        this.game_map = game_map;
        // 基本参数, 后期可以重新设置
        MAP_HEIGHT = (game_map.getSize().height) / PADDING;
        MAP_WIDTH = (game_map.getSize().width) / PADDING;
        isOccupied = new boolean[MAP_HEIGHT][MAP_WIDTH];
        // 设点的坐标从0开始 snake_length
        initSnake(SNAKE_LENGTH); 
        list_key = new ArrayList<>();
        map_food = new HashMap<>();
    }
    
    // 最初的蛇都设置其从左边移动到右边, 蛇头在地图中央
    private void initSnake(int snake_length) throws Exception{
        list_body = new ArrayList<>();
        for(int i=0; i<snake_length; i++){
            if(MAP_WIDTH/2 - i <0 ){
                throw new Exception("蛇的长度过长");
            }
            list_body.add(new Point(MAP_WIDTH/2 - i,
                                         MAP_HEIGHT/2));
            isOccupied[MAP_HEIGHT/2][MAP_WIDTH/2 - i] = true;
        }
        head_dir = DIR_RIGHT;
    }
    
    // 克隆一个头节点, 让其移动到下一步的位置
    private Point headMove(){
       Point point = (Point)list_body.get(0).clone();
            switch(head_dir){
                case DIR_LEFT: point.x = (point.x - 1 + MAP_WIDTH) % MAP_WIDTH;
                               break;
                case DIR_RIGHT: point.x = (point.x +1) % MAP_WIDTH;
                               break;
                case DIR_UP: point.y = (point.y - 1 + MAP_HEIGHT) % MAP_HEIGHT;
                               break;
                case DIR_DOWN: point.y = (point.y + 1) % MAP_HEIGHT;
                               break;
        }
        return point;
    }
    
    // 行为 "吃东西"
    // 每次更新两个数据与成员 isOccupied, list_body
    private void eat(Point point){
        // 克隆一个头节点, 移动它, 插入到最前面
        if(null != list_body){
            isOccupied[point.y][point.x] = true;
            list_body.add(0, point);
        }
    }  
    // 行为 "移动"
    // 每次更新两个数据与成员 isOccupied, list_body
    private void move(Point point){
        // 把尾部的节点去掉, 克隆一个头节点, 然后移动它
        // 最后插入到头部
        // 由于大部分操作相同,就直接调用了eat();
        if(null != list_body){
            eat(point);
            point = list_body.remove(list_body.size()-1);  
            isOccupied[point.y][point.x] = false;
            // 调用eat();   
        }
    }
    
    // 生成食物
    // 每次更新两个数据与成员 isOccupied, list_body
    private void generateFood(){
        // x, y 是 二维表示
        // position 是 一维表示
        int position = (int)(Math.random()*MAP_HEIGHT*MAP_WIDTH);
        int x = position % MAP_WIDTH;
        int y = position / MAP_WIDTH;
        while(isOccupied[y][x] == true){
            position = (position + 1) % (MAP_HEIGHT*MAP_WIDTH);
            x = position % MAP_WIDTH;
            y = position / MAP_WIDTH;  
        }
        isOccupied[y][x] = true;
        // key的值就用 position 来表示
        list_key.add(position);
        map_food.put(list_key.get(list_key.size()-1),
                new Point(x, y));
    }
    
    private int getKey(int x, int y){
        return x + (y * MAP_WIDTH);
    }
    
    private boolean isOccupiedAll(){
        for(boolean[] temps: isOccupied)
            for(boolean temp: temps)
                if(false == temp)
                    return false;
        return true;
    }
     
    // 开放接口 
    public boolean crawl(){
        if(isOccupiedAll()){
            // 所有位置已经填满
            // 游戏结束, 完成
            return false;
        }
        // 生成"食物"
        counter++;
        if(counter >= GENERATE_RATE){
            counter = 0;
            generateFood();
        }
        // 移动下一步有三种情况: 1. 吃到食物 2. 撞到自己 3. 简单移动
        //     判断是否吃到"食物"
        Point point = headMove();
        int key = getKey(point.x, point.y);       
        if(null != map_food.get(key)){      
            // 吃到"食物"
            score = score + 100;
            eat(point);
            list_key.remove(new Integer(key));
            map_food.remove(key);
        }else if(true == isOccupied[point.y][point.x]){
            // 撞到自己
            System.out.println(1);
            return false;
        }else{
           // 简单移动
            move(point); 
        }        
        return true;
    }
    
    // 开放接口
    public int getScore(){
        return score;
    }

    public GreedySnake() {
        this.game_map = null;
        this.MAP_HEIGHT = 0;
        this.MAP_WIDTH = 0;
        this.isOccupied = null;
    }
    
    // 开放接口 drawAll()
    public void drawAll(Graphics g){
        // 画出"食物"
        g.setColor(Color.GREEN);
        list_key.stream().forEach((key) -> {
            Point temp = map_food.get(key);
            g.fillRect(temp.x * PADDING, temp.y * PADDING , PADDING, PADDING);
        });
        g.setColor(Color.GRAY);
        // 如果JDK版本为1.8.0 执行新语法
        if(Util.getJDK_Version().equals("1.8.0")){
            list_body.stream().forEach((point) -> {
                g.drawRect(point.x * PADDING, point.y * PADDING , PADDING, PADDING);
            });
        }else{
            for(Point point: list_body){
                g.drawRect(point.x * PADDING, point.y * PADDING , PADDING, PADDING);
            }
        }
        // 把头画出红色
        Point point = list_body.get(0);
        g.setColor(Color.RED);
        g.fillRect(point.x * PADDING, point.y * PADDING , PADDING, PADDING);     
    }
         
    // 开放接口 setDir()
    public void setDir(int dir){
        if(dir == DIR_UP && head_dir == DIR_DOWN){
            return;
        }else if(dir == DIR_DOWN && head_dir == DIR_UP){
            return;
        }else if(dir == DIR_LEFT && head_dir == DIR_RIGHT){
            return;
        }else if(dir == DIR_RIGHT && head_dir == DIR_LEFT){
            return;
        }
        head_dir = dir;
    }
}
