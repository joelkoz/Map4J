package org.map4j.tinymq;


public class TinyMQDemo {

    public static TinyMQ<String> broker = new TinyMQ<String>();
    public static TinyMQ<Integer> broker2 = new TinyMQ<Integer>();
    
    public interface StringTopicSubscriber extends TinyMQ.ITopicSubscriber<String> {};
    
    public static final String TOPIC = "myTopic";

    // Example of a class subscribing by implementing the interface(s)...
    public static class Foo implements StringTopicSubscriber {

        public Foo() {
            broker.subscribe(TOPIC, this);
            
            // Since an object can't define two generic interfaces of the same
            // type, we'll use a lambda function to delegate to a local method
            // for the second interface...
            broker2.subscribe(TOPIC, (topic, val) -> {
                onPublishDelegated(topic, val);
            });
            
        }
        
        protected void finalize() throws Throwable {
            System.out.println("Foo finalized.");
        }
        
        public void onPublish(String topic, String msg) {
            System.out.println("class Foo got topic " + topic + " String payload " + msg);
        }


        public void onPublishDelegated(String topic, Integer msg) {
            System.out.println("class Foo got topic " + topic + " Integer payload " + msg);
        }
        
        
        public void publishMsg1() {
            broker.publish(TOPIC, "Foo published msg 1");
        }
    }
    
    
    public static void doFoo() {
        Foo foo = new Foo();
        foo.publishMsg1();
        broker2.publish(TOPIC, 123);

        // Comment this with System.gc() ALSO commented out to see how things go
        // can appear strange prior to garbage collection...
        // broker.unsubscribe(TOPIC, foo);
    }

    
    public static void main(String[] args) {

        broker.subscribe(TOPIC, (topic, msg) -> {
            System.out.println("Main subscriber got topic " + topic + " string payload " + msg);
        });

        broker2.subscribe(TOPIC, (topic, val) -> {
            System.out.println("Main subscriber got topic " + topic + " integer payload " + val);
        });
        
        
        broker.publish(TOPIC, "main() published msg #1");
        broker2.publish(TOPIC, 1);
        
        doFoo();
        
        // This will cause a garbage collection, which will cause the old Foo object to be finalized
        // and stop receiving messages. Comment this line out and run again to see how Foo sticks 
        // around and responds somewhat unexpectedly.  Keep it commented out and comment the
        // explicit unsubscribe() call in doFoo() to see how things behave normally, even without
        // garbage collection.
        System.gc();
        
        broker.publish(TOPIC, "main() published #2");
        broker2.publish(TOPIC, 2);
    }

}
