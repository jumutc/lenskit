/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.core;

import org.grouplens.grapht.Injector;
import org.grouplens.lenskit.GlobalItemRecommender;
import org.grouplens.lenskit.GlobalItemScorer;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.data.dao.DataAccessObject;

/**
 * Recommender implementation built on LensKit containers.  Recommenders built
 * with {@link LenskitRecommenderEngineFactory} will produce this type of
 * recommender.
 *
 * <p>The {@link Recommender} interface will meet most needs, so most users can
 * ignore this class.  However, if you need to inspect internal components of a
 * recommender (e.g. extract the item-item similarity matrix), this class and its
 * {@link #getComponent(Class)} method can be useful.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public class LenskitRecommender implements Recommender {
    private final Injector injector;
    private final DataAccessObject dao;
    private final boolean shouldCloseDao;

    /**
     * Create a new LensKit recommender.
     *
     * @param injector       The injector housing this recommender's configuration.
     * @param dao            The DAO backing this recommender session.
     * @param shouldCloseDao Whether the session should close the DAO.
     */
    public LenskitRecommender(Injector injector, DataAccessObject dao, boolean shouldCloseDao) {
        this.injector = injector;
        this.dao = dao;
        this.shouldCloseDao = shouldCloseDao;
    }

    /**
     * Get a particular component from the recommender session. Generally you
     * want to use one of the type-specific getters; this method only exists for
     * specialized applications which need deep access to the recommender
     * components.
     *
     * @param <T> The type of component to get.
     * @param cls The component class to get.
     * @return The instance of the specified component.
     */
    public <T> T get(Class<T> cls) {
        return injector.getInstance(cls);
    }

    @Override
    public ItemScorer getItemScorer() {
        return get(ItemScorer.class);
    }

    @Override
    public GlobalItemScorer getGlobalItemScorer() {
        return get(GlobalItemScorer.class);
    }

    @Override
    public RatingPredictor getRatingPredictor() {
        return get(RatingPredictor.class);
    }

    @Override
    public void close() {
        if (shouldCloseDao) {
            dao.close();
        }
    }

    /**
     * Get the DAO for this recommender session.
     *
     * @return The DAO, or {@var null} if this recommender is not connected
     *         to a DAO.  All LensKit recommenders are connected to DAOs; recommenders
     *         from other frameworks that are adapted to the LensKit API may not be.
     */
    public DataAccessObject getDataAccessObject() {
        return dao;
    }

    /**
     * Get the DAO.
     * @return The DAO.
     * @deprecated Use {@link #getDataAccessObject()}.
     */
    @Deprecated
    public DataAccessObject getRatingDataAccessObject() {
        return dao;
    }

    @Override
    public ItemRecommender getItemRecommender() {
        return get(ItemRecommender.class);
    }

    @Override
    public GlobalItemRecommender getGlobalItemRecommender() {
        return get(GlobalItemRecommender.class);
    }
}
