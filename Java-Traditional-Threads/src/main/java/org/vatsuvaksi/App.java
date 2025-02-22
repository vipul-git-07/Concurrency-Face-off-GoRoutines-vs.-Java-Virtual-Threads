package org.vatsuvaksi;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.vatsuvaksi.listener.RabbitMqListener;

@SpringBootApplication
public class App implements CommandLineRunner {
    private static final long MEMORY_THRESHOLD = 500 * 1024 * 1024; // 500 MB

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");

        try {
            Connection connection = factory.newConnection();
            RabbitMqListener listener = new RabbitMqListener(connection);
            listener.startListening();

            new Thread(() -> {
                while (true) {
                    long freeMemory = Runtime.getRuntime().freeMemory();
                    long totalMemory = Runtime.getRuntime().totalMemory();
                    long usedMemory = totalMemory - freeMemory;
                    if (usedMemory > MEMORY_THRESHOLD && listener.isConsuming()) {
                        listener.pauseConsumption();
                    } else if (usedMemory <= MEMORY_THRESHOLD && !listener.isConsuming()) {
                        listener.resumeConsumption();
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}