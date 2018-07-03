package com.fastfur.messaging.exercise;

import com.fastfur.messaging.data.Tweet;
import com.fastfur.messaging.producer.twitter.TwitterTopics;
import com.fastfur.messaging.serde.TweetSerde;
import com.fastfur.messaging.utils.CryptoUtil;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;

/**
 * Producer - TweetWithResponseProducer
 In this exercise you will have to encrypt tweets(text field only) from two topics :
 encode_tweets & got_responded, and then stream it. Affter the transformation,
 push the result to the encode_tweets topic.
 For your convenience you can use  the CryptoUtil Class.
 Hints:
 useful transformations and tools may be found here :
 https://kafka.apache.org/0110/javadoc/org/apache/kafka/streams/kstream/KStream.html
 https://kafka.apache.org/0102/javadoc/org/apache/kafka/streams/kstream/KStreamBuilder.html
 */
public class EncryptTweetExercise {
    public static void main(String[] args) {
        Properties config = new Properties();

        config.put( StreamsConfig.APPLICATION_ID_CONFIG, "my-first-tweet-ks1" );
        config.put( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092" );
        config.put( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        config.put( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TweetSerde.class );
        CryptoUtil cryptoUtil = new CryptoUtil();
        String key = "ezeon8547";


        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Tweet> stream = builder.stream( TwitterTopics.TWITTERS_TOPIC, Consumed.with( Serdes.String(), new TweetSerde() ) );
        KStream<String, Tweet> responseStream = builder.stream( TwitterTopics.GOT_RESPONDED_TOPIC, Consumed.with( Serdes.String(), new TweetSerde() ) );
        /**
        *  to( TwitterTopics.ENCODE_TWEETS, Produced.with( Serdes.String(), new TweetSerde() ) );
        */


        KafkaStreams streams = new KafkaStreams( builder.build(), config );
        streams.start();
    }
}
