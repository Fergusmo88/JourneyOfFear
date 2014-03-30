package game.util.client;

import game.enemy.EnemyPlayer;
import game.player.Player;
import game.util.server.DataPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class NetworkHandler {
    
    private DatagramSocket socket;
    private Thread get;
    private Thread send;
    private Player player;
    private volatile boolean running = true;
    private InetAddress ip;
    private int port = 0;
    private ArrayList<EnemyPlayer> enemies;
    
    private int myClientID;
    
    public NetworkHandler(String newIp, int newPort, Player localPlayer, ArrayList<EnemyPlayer> newEnemies) {
        this.enemies = newEnemies;
        try {
            this.ip = InetAddress.getByName(newIp);
        } catch (UnknownHostException e) {
            System.out.println("Unable to connect: " + e);
            return;
        }
        this.port = newPort;
        this.player = localPlayer;
    }
    
    public void start() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Error creating socket: " + e);
        }
        
        try {
            socket.send(new DatagramPacket(new byte[]{},0,ip,port));
        } catch (IOException e) {
            System.out.println("Error sending handshake data: " + e);
        }
        
        byte[] bytes = new byte[4];
        DatagramPacket recvPacket = new DatagramPacket(bytes,bytes.length);
        
        try {
            socket.receive(recvPacket);
        } catch (IOException e) {
            System.out.println("Error recieving handshake data: " + e);
        }
        
        DataPacket tempPacket = new DataPacket(recvPacket.getData());
        myClientID = tempPacket.getClient();
        
        get = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] recvData = new byte[DataPacket.MAX_SIZE];
                    DatagramPacket recvPacket = new DatagramPacket(recvData,recvData.length);
                    
                    while (running) {
                        socket.receive(recvPacket);
                        DataPacket recvDataPacket = new DataPacket(recvData);
                        
                        if (recvDataPacket.getClient() == myClientID)
                            continue;
                        
                        boolean updated = false;
                        
                        for (EnemyPlayer e : enemies) {
                            if (recvDataPacket.getClient() == e.client) {
                                e.x = recvDataPacket.get(DataPacket.X);
                                e.y = recvDataPacket.get(DataPacket.Y);
                                updated = true;
                                break;
                            }
                        }
                        
                        if (updated)
                            continue;
                        
                        enemies.add(new EnemyPlayer(recvDataPacket.get(DataPacket.X),recvDataPacket.get(DataPacket.Y),recvDataPacket.getClient()));
                    }
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error: " + e);
                } finally {
                    if (socket != null && !socket.isClosed())
                        socket.close();
                }
            }
        });
        get.start();

        send = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] sendData = new byte[DataPacket.MAX_SIZE];
                    while (running) {
                        sendData = player.getBytes(myClientID);
                        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,ip,port);
                        socket.send(sendPacket);
                    }
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error: " + e);
                } finally {
                    if (socket != null && !socket.isClosed())
                        socket.close();
                }
            }
        });
        send.start();
    }
}