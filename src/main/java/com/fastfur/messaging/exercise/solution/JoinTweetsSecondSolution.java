package com.fastfur.messaging.exercise.solution;

import com.fastfur.messaging.data.Tweet;
import com.fastfur.messaging.producer.Queries;
import com.fastfur.messaging.producer.twitter.TweetProducer;
import com.fastfur.messaging.producer.twitter.TwitterTopics;
import com.fastfur.messaging.serde.TweetSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Predicate;

import java.util.Properties;


public class JoinTweetsSecondSolution {

    public static final String INPUT_TOPIC_NAME = "twitters";
    private final static int ORIGINAL_STREAM = 0;
    private final static int RESPONSES_STREAM = 1;

    public static void main(String[] args) throws Exception {
        //produce data to the input topic
        TweetProducer tp = new TweetProducer();
        tp.produceTweets(INPUT_TOPIC_NAME, Queries.getQueries());

        Properties config = new Properties();
        config.put( StreamsConfig.APPLICATION_ID_CONFIG, "my-first-tweet-ks2" );
        config.put( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092" );
        config.put( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        config.put( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TweetSerde.class );

        //build the topology
        StreamsBuilder builder = new StreamsBuilder();
        //create original stream
        KStream<String, Tweet> tweetKStream = builder.stream( TwitterTopics.TWITTERS_TOPIC, Consumed.with( Serdes.String(), new TweetSerde() ) );
        //define predicates
        Predicate<String, Tweet> originalTweetPredicate = (k,v)-> v.getInReponseTo() == -1;
        Predicate<String, Tweet> responseTweetPredicate = (k,v)-> v.getInReponseTo() != -1;
        //create two streams based on the predicates
        KStream<String,Tweet> [] twoStreams = tweetKStream.branch(originalTweetPredicate, responseTweetPredicate);

        KStream<String, Tweet> originalStream =
                twoStreams[ORIGINAL_STREAM];
        KStream<String, Tweet> responseStream =
                twoStreams[RESPONSES_STREAM].selectKey((k,v) -> String.valueOf(v.getInReponseTo()));

        //finally join two streams
        originalStream.join(responseStream, (Tweet tweetOriginal, Tweet tweetResponse) -> tweetResponse.getCreatedAt().getTime() - tweetOriginal.getCreatedAt().getTime(),
                JoinWindows.of( 60*60*1000) )
                .foreach( (k, v) -> System.out.println( "key : " + k.toString() + " value : " + v ) );

        KafkaStreams streams = new KafkaStreams( builder.build(), config );
        streams.start();
    }
}
