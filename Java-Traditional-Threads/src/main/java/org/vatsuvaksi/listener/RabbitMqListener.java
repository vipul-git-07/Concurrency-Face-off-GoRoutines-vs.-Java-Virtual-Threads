package org.vatsuvaksi.listener;

import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.vatsuvaksi.executor.CustomExecutor;
import org.vatsuvaksi.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.vatsuvaksi.repository.UserRepository;
import org.vatsuvaksi.service.UserProcessor;

import java.util.concurrent.CompletableFuture;



public class RabbitMqListener {
    private static final String QUEUE_NAME = "my_queue";
    private final Channel channel;
    private String consumerTag;
    private volatile boolean consuming = false;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean paused = false;





    public RabbitMqListener(Connection connection) throws Exception {
        this.channel = connection.createChannel();
        this.channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    }

    public void startListening() {
        if (!consuming) {
            try {
                consuming = true;
                System.out.println("Starting consumption...");
                this.channel.basicQos(Runtime.getRuntime().availableProcessors());
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    if (paused) {
                        System.out.println("Consumption paused. Requeuing message.");
                        try {
                            Thread.sleep(1000);
                            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    String jsonMessage = new String(delivery.getBody(), "UTF-8");

                    try {
                        User user = objectMapper.readValue(jsonMessage, User.class);
                        CompletableFuture.supplyAsync(new UserProcessor(user), CustomExecutor.getExecutorService())
                                .handle((result, ex) -> {
                                    try {
                                        if (ex != null || !result) {
                                            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                                        } else {
                                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                });

                    } catch (Exception e) {
                        e.printStackTrace();
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false); // NACK and discard
                    }
                };

                consumerTag = channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void pauseConsumption() {
        System.out.println("Pausing consumption (application-level)...");
        paused = true;
    }

    // Reset the paused flag to resume processing.
    public void resumeConsumption() {
        System.out.println("Resuming consumption...");
        paused = false;
    }

    public boolean isConsuming() {
        return consuming;
    }

}
