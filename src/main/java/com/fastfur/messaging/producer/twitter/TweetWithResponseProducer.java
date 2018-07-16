package com.fastfur.messaging.producer.twitter;

import com.fastfur.messaging.data.Constant;
import com.fastfur.messaging.data.Tweet;

import twitter4j.Status;

import java.util.List;


public class TweetWithResponseProducer extends TweetProducer {

    public TweetWithResponseProducer() {
        super.init();
    }

    public void produceTweets(String[] queries) throws Exception {
        for (String query : queries) {
            for (Status status : searchTweets( query )) {

                produce( new Tweet( String.valueOf( status.getId() ), status ), TwitterTopics.TWITTERS_TOPIC );
                if (status.getInReplyToStatusId() != -1) {
                    long statusId = status.getInReplyToStatusId();
                    try {
                        produce( new Tweet( String.valueOf( statusId ), twitter.showStatus( statusId ) ), TwitterTopics.GOT_RESPONDED_TOPIC );
                    } catch (Exception ex) {
                        System.out.println( "error while trying to get status : " + statusId );
                    }
                }
            }
        }
    }

    public List<Status> searchTweets(String username) throws Exception {
        List<Status> result = twitter.getUserTimeline( username );
        return result;
    }

    public static void main(String[] args) throws Exception {
        TweetWithResponseProducer tp = new TweetWithResponseProducer();
        tp.produceTweets( Constant.TOP_TWEETS );

    }
}
