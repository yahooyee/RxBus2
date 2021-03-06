package com.michaelflisar.rxbus2;

import com.michaelflisar.rxbus2.exceptions.RxBusEventCastException;
import com.michaelflisar.rxbus2.exceptions.RxBusEventIsNullException;
import com.michaelflisar.rxbus2.exceptions.RxBusKeyIsNullException;
import com.michaelflisar.rxbus2.rx.RxQueueKey;

import org.reactivestreams.Processor;

/**
 * Created by Michael on 24.02.2017.
 */

public class RxBusSenderBuilder
{
    private Class<?> mCast = null;
    private Object mKey = null;
    private boolean mSendToDefaultBus = false;

    RxBusSenderBuilder()
    {
    }

    /**
     * Force casting an event to the provided class so that any observer of the provided class will retrieve this event
     * Useful for sending many sub classes to one base class observer
     * Sending an event that is not assignable by this class will throw an exception.
     * <p>
     * @param  cast  the class to withCast the event to
     * @return an {@link RxBusSenderBuilder} for chaining additional calls before calling {@link RxBusSenderBuilder#send(Object)}
     */
    public RxBusSenderBuilder withCast(Class<?> cast)
    {
        mCast = cast;
        return this;
    }

    /**
     * Force sending an event to observers of the provided withKey only
     * <p>
     * @param  key the withKey this event should be broadcasted to
     * @return an {@link RxBusSenderBuilder} for chaining additional calls before calling {@link RxBusSenderBuilder#send(Object)}
     */
    public RxBusSenderBuilder withKey(Integer key)
    {
        RxBusKeyIsNullException.checkKey(key);
        mKey = key;
        return this;
    }

    /**
     * Force sending an event to observers of the provided withKey only
     * <p>
     * @param  key the withKey this event should be broadcasted to
     * @return an {@link RxBusSenderBuilder} for chaining additional calls before calling {@link RxBusSenderBuilder#send(Object)}
     */
    public RxBusSenderBuilder withKey(String key)
    {
        RxBusKeyIsNullException.checkKey(key);
        mKey = key;
        return this;
    }

    /**
     * Force sending an event the default class as well, even if a withKey is provider
     * <p>
     * @return an {@link RxBusSenderBuilder} for chaining additional calls before calling {@link RxBusSenderBuilder#send(Object)}
     */
    public RxBusSenderBuilder withSendToDefaultBus()
    {
        mSendToDefaultBus = true;
        return this;
    }

    /**
     * Send an event to the bus, applying all already chained settings
     * <p>
     * @param  event  the event that should be broadcasted to the bus
     * @return true, if the item is send to the bus, false otherwise (false will be returned if noone is listening to this event)
     */
    public synchronized boolean send(Object event)
    {
        RxBusEventIsNullException.checkEvent(event);
        if (mCast != null)
            RxBusEventCastException.checkEvent(event, mCast);

        boolean send = false;

        // 1) send to simple unbound bus
        if (mKey == null || mSendToDefaultBus)
        {
            Processor processor = RxBus.getInstance().getProcessor(mCast == null ? event.getClass() : mCast, false);

            // only send event, if processor exists => this means someone has at least once subscribed to it
            if (processor != null)
            {
                if (mCast == null)
                    processor.onNext(event);
                else
                    processor.onNext(mCast.cast(event));
                send = true;
            }
        }

        // 2) send to withKey bound bus
        if (mKey != null)
        {
            Processor processor = null;

            if (mKey instanceof String)
                processor = RxBus.getInstance().getProcessor(new RxQueueKey(mCast == null ? event.getClass() : mCast, (String)mKey), false);
            else if (mKey instanceof Integer)
                processor = RxBus.getInstance().getProcessor(new RxQueueKey(mCast == null ? event.getClass() : mCast, (Integer)mKey), false);

            // only send event, if processor exists => this means someone has at least once subscribed to it
            if (processor != null)
            {
                if (mCast == null)
                    processor.onNext(event);
                else
                    processor.onNext(mCast.cast(event));
                send = true;
            }
        }

        return send;
    }
}
