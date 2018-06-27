package com.fastfur.messaging.producer;

import com.fastfur.messaging.data.Identity;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.UUID;

public class BaseProducer {

    protected Producer producer;
    protected Properties properties  = null;



    protected  < K, T extends Identity> void produce(T record, String topic){

        producer.send(new ProducerRecord(topic, record.getKey(), record));
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    protected String createUUID() {
        String uuid  = UUID.randomUUID().toString();
        return uuid;
    }



    public Properties initProps(){
        properties = new Properties();       //172.18.0.3
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("acks", "1");
        properties.put("retries", 0);
        properties.put("batch.size", 512);
        properties.put("linger.ms", 1);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        return properties;
    }

    protected  void close(){
        System.out.println("---------producer close method is called!!----------");
        producer.close();    }
}
