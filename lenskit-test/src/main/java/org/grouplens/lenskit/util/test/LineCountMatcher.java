/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.util.test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.io.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LineCountMatcher extends BaseMatcher<File> {
    private final Matcher<? extends Integer> lineCount;

    public LineCountMatcher(Matcher<? extends Integer> m) {
        lineCount = m;
    }

    @Override
    public boolean matches(Object o) {
        return o instanceof File && lineCount.matches(getLineCount((File) o));
    }

    private int getLineCount(File file) {
        try (Reader reader = new FileReader(file);
             BufferedReader lines = new BufferedReader(reader)) {
            int n = 0;
            while (lines.readLine() != null) {
                n++;
            }
            return n;
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("file " + file + " not found", ex);
        } catch (IOException ex) {
            throw new RuntimeException("error reading file " + file, ex);
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("file with line count ")
                   .appendDescriptionOf(lineCount);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        if (item instanceof File) {
            int lines = getLineCount((File) item);
            description.appendText("had " + lines + " lines");
        } else {
            description.appendText("was non-file object " + item);
        }
    }
}
