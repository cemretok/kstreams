package com.fastfur.messaging.exercise.solution;

import com.fastfur.messaging.data.Tweet;
import com.fastfur.messaging.producer.twitter.TwitterTopics;
import com.fastfur.messaging.serde.TweetSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;


public class DevicesTopologySolution {


    public DevicesTopologySolution() {
    }

    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "my-first-tweet-ks1");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TweetSerde.class);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Tweet> stream = builder.stream(TwitterTopics.TWITTERS_TOPIC, Consumed.with(Serdes.String(), new TweetSerde()));
        KStream<String, Tweet> deviceStream = builder.stream(TwitterTopics.DEVICES_TOPIC, Consumed.with(Serdes.String(), new TweetSerde()));
        //repartition the original stream in such way that the new stream key is a "device" the tweet is sent from
        //send repartitioned stream to the new topic
        stream
                .selectKey((k, v) -> v.deviceFromSource())
                .to(TwitterTopics.DEVICES_TOPIC, Produced.with(Serdes.String(), new TweetSerde()));

        //read the repartitioned stream from the new topic and group by device and then count
        KTable<String, Long> deviceKtable = deviceStream.groupByKey().count();
        deviceKtable.toStream().foreach((k, v) -> System.out.println("Device-> " + k + "  number -> " + v));

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();
    }

}
