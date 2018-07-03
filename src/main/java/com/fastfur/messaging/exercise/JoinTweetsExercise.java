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
import org.omg.PortableInterceptor.LOCATION_FORWARD;

import java.time.Duration;
import java.util.Properties;



public class JoinTweetsExercise {


    /**
     In this exercise you will have to calculate the time difference
     between a response to a tweet and the original tweet.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {


        Properties config = new Properties();
        config.put( StreamsConfig.APPLICATION_ID_CONFIG, "my-first-tweet-ks1" );
        config.put( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092" );
        config.put( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        config.put( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TweetSerde.class );


        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Tweet> stream = builder.stream( TwitterTopics.TWITTERS_TOPIC, Consumed.with( Serdes.String(), new TweetSerde() ) );
        KStream<Long, Tweet> deviceStream = builder.stream( TwitterTopics.GOT_RESPONDED_TOPIC, Consumed.with( Serdes.Long(), new TweetSerde() ) );
        /**
         .foreach( (k, v) -> System.out.println( "key" + k.toString() + " value : " + v ) );
         */




        KafkaStreams streams = new KafkaStreams( builder.build(), config );
        streams.start();







    }


}
