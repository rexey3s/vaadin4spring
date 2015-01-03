package org.vaadin.spring.navigator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;

/**
 * Base class for presentation.
 * Derivatives must be annotated with {@link VaadinPresenter}.
 * Works with {@link EventBus} to subscribe to events and set model data
 * on an implementor of {@link com.vaadin.navigator.View}.
 *
 * @author Chris Phillipson (fastnsilver@gmail.com)
 *
 * @param <V> an implementation of {@link com.vaadin.navigator.View}
 */
public abstract class Presenter<V extends View> {

    private static Logger logger = LoggerFactory.getLogger(Presenter.class);

    @Autowired
    private SpringViewProvider viewProvider;

    @Autowired
    private EventBus eventBus;
    
    private String viewName;

    @PostConstruct
    protected void init() {
        eventBus.subscribe(this);
        viewName = getViewName();
    }

    public EventBus getEventBus() {
        return this.eventBus;
    }
    
    private String getViewName() {
        String result = null;
        Class<?> clazz = getClass();
        if (clazz.isAnnotationPresent(VaadinPresenter.class)) {
            VaadinPresenter vp = clazz.getAnnotation(VaadinPresenter.class);
            result = vp.viewName();
        } else {
            logger.error("Presenter [{}] does not have a @VaadinPresenter annotation!", clazz.getSimpleName());
        }
        return result;
    }

    /**
     * Hands back the View that this Presenter works with
     * A match is made if the ViewProvider finds a VaadinView annotated View whose name matches Presenter's viewName
     * @return an implementor of {@link View}
     */
    @SuppressWarnings("unchecked")
    public V getView() {
        V result = null;
        if (viewName != null) {
            result = (V) viewProvider.getView(viewName);
        }
        return result;
    }


    /**
     * Allows this Presenter to (possibly) work with other views
     * @return a ViewProvider
     */
    public ViewProvider getViewProvider() {
        return this.viewProvider;
    }

    @PreDestroy
    // It's good manners to do this, even though we should be automatically unsubscribed
    // when the UI is garbage collected
    void destroy() {
        getEventBus().unsubscribe(this);
    }

}
