package com.fastfur.messaging.exercise;

import com.fastfur.messaging.data.Tweet;
import com.fastfur.messaging.producer.twitter.TwitterTopics;
import com.fastfur.messaging.serde.TweetSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;


/**
 In this exercise you will have to implement a topology that will print the most popular
 tweet in each minute for each language. The time window should be for the last 10 minutes.
 Filter the tweets such that only tweets with 10 likes and above are passed
 */
public class PopularTweetsExercise {


    public static void main(String[] args) throws Exception {


        Properties config = new Properties();
        config.put( StreamsConfig.APPLICATION_ID_CONFIG, "my-first-tweet-ks1" );
        config.put( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092" );
        config.put( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        config.put( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TweetSerde.class );


        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Tweet> stream = builder.stream( TwitterTopics.TWITTERS_TOPIC, Consumed.with( Serdes.String(), new TweetSerde() ) );
            /**
            *foreach( (k, v) -> System.out.println( "start -> " + k.window().start() +  "  key -> " + k.key() ) );
             */

        KafkaStreams streams = new KafkaStreams( builder.build(), config );
        streams.start();


    }






}
