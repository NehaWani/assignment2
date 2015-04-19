package demo;

import java.util.Properties;

import org.springframework.stereotype.Component;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

@Component
public class KafkaProducer {

    private static Producer<Integer, String> producer;
    private final Properties properties = new Properties();
    String topic = "cmpe273-topic";

    public KafkaProducer() {
        properties.put("metadata.broker.list", "54.149.84.25:9092");
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        properties.put("request.required.acks", "1");
        producer = new Producer<Integer, String>(new ProducerConfig(properties));
    }

    public void sendMessage(String msg) {
        KeyedMessage<Integer, String> data = new KeyedMessage<Integer, String>(topic, msg);
        producer.send(data);

    }

    public void closeProducer(){
        producer.close();
    }
   
}
