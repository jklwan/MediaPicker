package com.chends.media.picker.decoder;

/**
 * @author chends create on 2019/9/20.
 */
public class AnimDecoderFactory<T extends AnimDecoder<?>> {
    private final Class<? extends T> clazz;

    public AnimDecoderFactory(Class<? extends T> clazz) {
        this.clazz = clazz;
    }

    public AnimDecoder<?> make() throws InstantiationException, IllegalAccessException {
        return clazz.newInstance();
    }
}
