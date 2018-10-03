package com.wuchangi;

/*
 * @program: MFQ
 * @description: MFQSimulation
 * @author: WuchangI
 * @create: 2018-05-20-22-04
 **/


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.Arrays;
import java.util.LinkedList;


public class MFQSimulation
{
    private static JFrame frame = new JFrame("进程调度模拟（多级反馈队列）");
    private static JPanel panel = new JPanel();
    private static JScrollPane scrollPane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

    //菜单组件
    private static JMenuBar menuBar = new JMenuBar();
    private static JMenu processSettingsMenu = new JMenu("Process Settings");
    private static JMenuItem createProcessItem = new JMenuItem("Create A Process");
    private static JMenuItem startMFQItem = new JMenuItem("Start Scheduling");
    private static JMenuItem stopMFQItem = new JMenuItem("Stop Scheduling");
    private static JMenuItem setTimeSliceItem = new JMenuItem("Set Time Slice");
    private static JMenuItem exitSystemItem = new JMenuItem("Exit");
    private static JMenu helpMenu = new JMenu("Help");
    private static JMenuItem aboutItem = new JMenuItem("About");

    //设置优先级最高(即49)的队列的时间片大小默认值（单位：秒）
    public static double timeSlice = 0.5;

    public static double PCBsQueuesTimeSlice[] = new double[50];

    //多级反馈队列
    public static PCBsQueue[] PCBsQueues = new PCBsQueue[50];

    //记录已经使用的pid
    public static int[] pidsUsed = new int[101];

    //当前内存中的进程数
    public static int currentPCBsNum = 0;

    //内存中能够容纳的最大进程数（这里取决于可分配的pid的个数）
    public static final int PCBS_MAX_NUM = 100;

    //是否停止调度
    public static boolean isStopScheduling;

    //很短的main函数
    public static void main(String[] args)
    {
        new MFQSimulation().initWindow();
    }



    //执行窗口初始化
    public void initWindow()
    {
        //设置窗口风格为Windows风格
        setWindowsStyle();

        //创建菜单栏
        processSettingsMenu.add(createProcessItem);
        processSettingsMenu.addSeparator();
        processSettingsMenu.add(startMFQItem);
        processSettingsMenu.addSeparator();
        processSettingsMenu.add(stopMFQItem);
        processSettingsMenu.addSeparator();
        processSettingsMenu.add(setTimeSliceItem);
        processSettingsMenu.addSeparator();
        processSettingsMenu.add(exitSystemItem);

        helpMenu.add(aboutItem);

        menuBar.add(processSettingsMenu);
        menuBar.add(helpMenu);

        frame.setJMenuBar(menuBar);

        initMemory();

        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        frame.setContentPane(scrollPane);
        frame.setSize(800, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);


        //为控件绑定监听器
        setComponentsListeners();
    }

    //设置Swing的控件显示风格为Windows风格
    public static void setWindowsStyle()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();
        }

    }

    //初始化相关内存参数
    public static void initMemory()
    {
        currentPCBsNum = 0;

        Arrays.fill(pidsUsed, 1, 101, 0);

        for(int i = 0; i < PCBsQueues.length; i++)
        {
            PCBsQueues[i] = new PCBsQueue(i);
        }

        for(int i = PCBsQueuesTimeSlice.length - 1; i >= 0; i--)
        {
            //队列优先级每降一级，时间片增加0.1秒
            PCBsQueuesTimeSlice[i] = timeSlice;
            timeSlice += 0.1;
        }
    }

    //给窗口中所有控件绑定监听器
    public static void setComponentsListeners()
    {
        createProcessItem.setAccelerator(KeyStroke.getKeyStroke('F', InputEvent.CTRL_MASK));
        createProcessItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                createProcess();
            }
        });


        startMFQItem.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_MASK));
        startMFQItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                startMFQSimulation();
            }
        });

        stopMFQItem.setAccelerator(KeyStroke.getKeyStroke('P', InputEvent.CTRL_MASK));
        stopMFQItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                stopMFQSimulation();
            }
        });

        setTimeSliceItem.setAccelerator(KeyStroke.getKeyStroke('T', InputEvent.CTRL_MASK));
        setTimeSliceItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setTimeSlice();
            }
        });


        exitSystemItem.setAccelerator(KeyStroke.getKeyStroke('E', InputEvent.CTRL_MASK));
        exitSystemItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });

        aboutItem.setAccelerator(KeyStroke.getKeyStroke('A', InputEvent.CTRL_MASK));
        aboutItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(frame, "Multilevel feedback queue simulation application (1.0 version)\n\nCopyright © 2018, 余梓权, All Rights Reserved.");
            }
        });

    }

    //创建新进程
    public static void createProcess()
    {
        if(currentPCBsNum == PCBS_MAX_NUM)
        {
            JOptionPane.showMessageDialog(frame,"The current memory space is full and cannot create a new process！");
        }
        else
        {
            currentPCBsNum++;

            int randomPid = 1 + (int)(Math.random() * ((100 - 1) + 1));

            while(pidsUsed[randomPid] == 1)
            {
                randomPid = 1 + (int)(Math.random() * ((100 - 1) + 1));
            }

            pidsUsed[randomPid] = 1;

            int randomPriority = 0 + (int)(Math.random() * ((49 - 0) + 1));
            int randomLife = 1 + (int)(Math.random() * ((5 - 1) + 1));

            PCB pcb = new PCB(randomPid, "Ready", randomPriority, randomLife);

            LinkedList<PCB> queue = PCBsQueues[randomPriority].getQueue();
            queue.offer(pcb);
            PCBsQueues[randomPriority].setQueue(queue);

            showPCBQueues(PCBsQueues);
        }
    }

    //开始调度
    public static void startMFQSimulation()
    {
        isStopScheduling = false;

        //更新界面操作必须借助多线程来实现
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                //当前内存中还留有进程未执行
                while(currentPCBsNum!=0 && !isStopScheduling)
                {
                    for(int i = PCBsQueues.length - 1; i >= 0; i--)
                    {
                        LinkedList<PCB> queue = PCBsQueues[i].getQueue();

                        if (queue.size() > 0)
                        {
                            //读取该队列首个PCB
                            PCB pcb = queue.element();
                            pcb.setStatus("Running");
                            showPCBQueues(PCBsQueues);

                            int pid = pcb.getPid();
                            int priority = pcb.getPriority();
                            int life = pcb.getLife();
                            priority = priority / 2;
                            life = life - 1;

                            //通过延时一个时间片来模拟该进程的执行
                            try
                            {
                                Thread.sleep((int)(PCBsQueuesTimeSlice[priority] * 1000));
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }

                            //若该进程执行完成
                            if(life == 0)
                            {
                                //移除该队列的首个PCB
                                queue.poll();
                                pidsUsed[pid] = 0;
                                currentPCBsNum--;
                            }
                            //若该进程还未执行完成,则改变其PCB的相关参数,并插入其优先级所对应的队列尾部
                            else
                            {
                                //移除该队列的首个PCB
                                queue.poll();

                                pcb.setPriority(priority);
                                pcb.setLife(life);
                                pcb.setStatus("Ready");
                                LinkedList<PCB> nextQueue = PCBsQueues[priority].getQueue();
                                nextQueue.offer(pcb);
                                PCBsQueues[priority].setQueue(nextQueue);
                            }

                            break;
                        }
                    }
                }

                initMemory();
                showPCBQueues(PCBsQueues);
                //所有进程均执行完成，进程调度完成
                JOptionPane.showMessageDialog(frame, "Process scheduling over!");
            }
        }).start();

    }

    //强制结束进程调度
    public static void stopMFQSimulation()
    {
        isStopScheduling = true;
        initMemory();
    }

    //设置时间片大小
    public static void setTimeSlice()
    {
        String inputMsg = JOptionPane.showInputDialog(frame, "Please input your time slice(seconds)：", 0.5);

        double timeSliceInput = Double.parseDouble(inputMsg);

        while(timeSliceInput <= 0)
        {
            JOptionPane.showMessageDialog(frame, "Time  Slice is illegal, Please set time slice again!");
            inputMsg = JOptionPane.showInputDialog(frame, "Please input your time slice(seconds)：", "Set Time Slice", JOptionPane.PLAIN_MESSAGE);
            timeSliceInput = Integer.parseInt(inputMsg);
        }

        timeSlice = timeSliceInput;
    }

    //显示内存中的多级反馈队列
    public static void showPCBQueues(PCBsQueue[] PCBsQueues)
    {
        int queueLocationY = 0;
        JPanel queuesPanel = new JPanel();

        for(int i = PCBsQueues.length - 1; i >= 0; i--)
        {
            LinkedList<PCB> queue = PCBsQueues[i].getQueue();

            if (queue.size() > 0)
            {
                //创建一个PCB队列
                JPanel PCBsQueue = new JPanel();
                // PCBsQueue.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                PCBsQueue.setLayout(new FlowLayout(FlowLayout.LEFT));
                PCBsQueue.setBounds(0, queueLocationY, 800, 700);

                queueLocationY += 50;

                //创建队列前面的优先级提示块
                JLabel PCBsQueuePriorityLabel = new JLabel("Priority of queue: " + String.valueOf(i));
                PCBsQueuePriorityLabel.setOpaque(true);
                PCBsQueuePriorityLabel.setBackground(Color.RED);
                PCBsQueuePriorityLabel.setForeground(Color.YELLOW);

                JPanel PCBsQueuePriorityBlock = new JPanel();
                PCBsQueuePriorityBlock.add(PCBsQueuePriorityLabel);

                PCBsQueue.add(PCBsQueuePriorityBlock);

                for (PCB pcb : queue)
                {

                    //JLabel默认情况下是透明的所以直接设置背景颜色是无法显示的，必须将其设置为不透明才能显示背景

                    //设置pid标签
                    JLabel pidLabel = new JLabel("Pid: " + String.valueOf(pcb.getPid()));
                    pidLabel.setOpaque(true);
                    pidLabel.setBackground(Color.GREEN);
                    pidLabel.setForeground(Color.RED);
                    pidLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    //设置status标签
                    JLabel statusLabel = new JLabel("Status: " + pcb.getStatus());
                    statusLabel.setOpaque(true);
                    statusLabel.setBackground(Color.GREEN);
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    //设置priority标签
                    JLabel priorityLabel = new JLabel("Priority: " + String.valueOf(pcb.getPriority()));
                    priorityLabel.setOpaque(true);
                    priorityLabel.setBackground(Color.GREEN);
                    priorityLabel.setForeground(Color.RED);
                    priorityLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    //设置life标签
                    JLabel lifeLabel = new JLabel("Life: " + String.valueOf(pcb.getLife()));
                    lifeLabel.setOpaque(true);
                    lifeLabel.setBackground(Color.GREEN);
                    lifeLabel.setForeground(Color.RED);
                    lifeLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    //绘制一个PCB
                    JPanel PCBPanel = new JPanel();
                    PCBPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    PCBPanel.setBackground(Color.BLUE);
                    PCBPanel.add(pidLabel);
                    PCBPanel.add(statusLabel);
                    PCBPanel.add(priorityLabel);
                    PCBPanel.add(lifeLabel);

                    //将PCB加入队列
                    PCBsQueue.add(new DrawLinePanel());
                    PCBsQueue.add(PCBPanel);
                }

                queuesPanel.add(PCBsQueue);
            }
        }


        //设置queuesPanel中的所有PCB队列（PCBsQueue组件）按垂直方向排列
        BoxLayout boxLayout = new BoxLayout(queuesPanel, BoxLayout.Y_AXIS);
        queuesPanel.setLayout(boxLayout);

        queuesPanel.setSize(800, 700);

        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.removeAll();
        panel.add(queuesPanel);
        panel.updateUI();
        panel.repaint();
    }

}



//绘制直线类
class DrawLinePanel extends JPanel
{
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.drawLine(0, this.getSize().height / 2, this.getSize().width, this.getSize().height/2);

    }

}


