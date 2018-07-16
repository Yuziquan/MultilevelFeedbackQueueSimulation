package com.wuchangi;

/*
 * @program: MFQ
 * @description: PCB
 * @author: WuchangI
 * @create: 2018-05-20-22-04
 **/

//进程控制块类
public class PCB
{
    //进程标识符
    private int pid;

    //进程状态标识
    private String status;

    //进程优先级
    private int priority;

    //进程生命周期
    private int life;

    public PCB()
    {
    }

    public PCB(int pid, String status, int priority, int life)
    {
        this.pid = pid;
        this.status = status;
        this.priority = priority;
        this.life = life;
    }

    public int getPid()
    {
        return pid;
    }

    public void setPid(int pid)
    {
        this.pid = pid;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public int getLife()
    {
        return life;
    }

    public void setLife(int life)
    {
        this.life = life;
    }
}
