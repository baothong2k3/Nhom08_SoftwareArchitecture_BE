package iuh.fit.se.emailservice.configs;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Value("${app.rabbit.queue-name}")
    private String queueName;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private String port;

    @Value("${spring.rabbitmq.username}")
    private String userName;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean
    public Queue queue() {
        return new Queue(queueName, false, false, false);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host);
        cachingConnectionFactory.setUsername(userName);
        cachingConnectionFactory.setPassword(password);
        cachingConnectionFactory.setPort(Integer.parseInt(port));
        cachingConnectionFactory.setHost(host);
        return cachingConnectionFactory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
//        factory.setConcurrentConsumers(3);  // Số lượng consumer đồng thời
//        factory.setMaxConcurrentConsumers(10);  // Số lượng consumer tối đa
//        factory.setPrefetchCount(10);  // Số lượng message mà mỗi consumer có thể nhận trước khi gửi ack
        return factory;
    }
}
