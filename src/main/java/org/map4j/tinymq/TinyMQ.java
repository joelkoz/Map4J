package org.map4j.tinymq;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * TinyMQ is an ultra-small embeddable message broker that supports a simple public/subscribe message
 * broker. Subscribers subscribe to topics by calling subscribe(). Weak references are held to subscribers
 * thus unsubscribing to a topic is not necessary: subscriptions will be cleaned up automatically when the
 * broker detects the subscriber no longer exists.
 * <p>Publishers publish topics by calling publish(). All subscribers will be notified immediately in
 * the current thread.
 * 
 * @author Joel Kozikowski
 *
 */
public class TinyMQ<TPayload> {

    /**
     * Subscribers wishing to be notified when a topic is published should
     * call the subscribe() method, passing any object that implements this
     * ITopicSubscriber interface. Most frequently, the subscriber
     * object is a lambda function.
     */
    public interface ITopicSubscriber<TPayload> {
        /**
         * Called whenever a topic that has been subscribed to has been
         * published.
         * @param topic The complete topic name that was published
         * @param payload The payload that accompanies the topic
         */
        public void onPublish(String topic, TPayload payload);
    }
    
    
    public class SubscriberEntry extends WeakReference<ITopicSubscriber<TPayload>> {
        SubscriberEntry(ITopicSubscriber<TPayload> ref) {
            super(ref);
        }
    }
    
    public class SubscriberList extends ArrayList<SubscriberEntry> {} 

    private HashMap<String, SubscriberList> subscriptions;

    
    public TinyMQ() {
       subscriptions = new HashMap<String, SubscriberList>();
    }
    

    /**
     * Subscribes the specified subscriber to the specified topic. The
     * onPublish() method of the subscriber will be called whenever 
     * the publish() method of this broker is called with the specified topic.
     */
    public void subscribe(String topic, ITopicSubscriber<TPayload> subscriber) {
        SubscriberList sList = subscriptions.get(topic);
        if (sList == null) {
            sList = new SubscriberList();
            subscriptions.put(topic, sList);
        }
        sList.add(new SubscriberEntry(subscriber));
    }
    

    public void unsubscribe(String topic, ITopicSubscriber<TPayload> subscriber) {
        SubscriberList sList = subscriptions.get(topic);
        if (sList != null) {
            Iterator<SubscriberEntry> itr = sList.iterator();
            while (itr.hasNext()) {
                SubscriberEntry sub2 = itr.next();
                if (sub2.get() != null) {
                    if (sub2.get().equals(subscriber)) {
                        itr.remove();
                    }
                }
            }
        }
    }
    
    
    
    /**
     * Publishes the specified topic by calling all subscriber's onPublish()
     * method with the specified payload.
     */
    public void publish(String topic, TPayload payload) {
        SubscriberList sList = subscriptions.get(topic);
        if (sList != null) {
            // Iterate over all subscribers.  If any subscriber is found to be
            // missing via garbage collection, remove them from the subscriber list
            Iterator<SubscriberEntry> itr = sList.iterator();
            while (itr.hasNext()) {
                SubscriberEntry subscriber = itr.next();
                if (subscriber.get() != null && !subscriber.isEnqueued()) {
                    // The subscriber still exists!  Publish the topic...
                    subscriber.get().onPublish(topic, payload);
                }
                else {
                    // The weak reference is gone now. Just remove it...
                    itr.remove();
                }
            }
        }        
    }

}
