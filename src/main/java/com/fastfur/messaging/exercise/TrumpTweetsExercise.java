package com.fastfur.messaging.exercise;

import com.fastfur.messaging.data.Tweet;
import com.fastfur.messaging.producer.Queries;
import com.fastfur.messaging.producer.twitter.TweetProducer;
import com.fastfur.messaging.producer.twitter.TwitterTopics;
import com.fastfur.messaging.serde.TweetSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Locale;
import java.util.Properties;

/**
 * In this exercise you will have to create new stream contain only tweets that contains
 * 'trump' in their user name . The new stream key will be the user name.
 */

public class TrumpTweetsExercise {
    public static final String INPUT_TOPIC_NAME = "twitters";

    public static void main(String[] args) throws Exception {

        Properties config = new Properties();
        config.put( StreamsConfig.APPLICATION_ID_CONFIG, "my-first-tweet-ks2" );
        config.put( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092" );
        config.put( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        config.put( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TweetSerde.class );
        StreamsBuilder builder = new StreamsBuilder();

        //produce data into Kafka topic (twitters)
        TweetProducer tp = new TweetProducer();
        tp.produceTweets(INPUT_TOPIC_NAME, Queries.getQueries());

        KStream<String, Tweet> stream = builder.stream( TwitterTopics.TWITTERS_TOPIC, Consumed.with( Serdes.String(), new TweetSerde() ) );
        //your code here....

        //uncomment to print result
        //for debugging purpose you can just replace "to" by "print" in order to print into console
        //rather than sending the data into Kafka topic
        /**
         .to( TwitterTopics.TRUMP_TWEETS, Produced.with( Serdes.String(), new TweetSerde() ) );
         */

        stream.filter( (k,v) ->  v.getName().toLowerCase().contains("trump")).selectKey((k,v) -> v.getName()).print();


        KafkaStreams streams = new KafkaStreams( builder.build(), config );
        streams.start();
    }
}
