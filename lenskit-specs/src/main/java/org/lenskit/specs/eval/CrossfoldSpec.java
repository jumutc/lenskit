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
package org.lenskit.specs.eval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.lenskit.specs.AbstractSpec;
import org.lenskit.specs.data.DataSourceSpec;
import org.lenskit.specs.data.PackedDataSourceSpec;
import org.lenskit.specs.data.TextDataSourceSpec;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification for running a crossfold operation.
 *
 * <p>The spec class contains some of the logic for determining output file locations so that this information is
 * available without actually running the crossfold.</p>
 */
public class CrossfoldSpec extends AbstractSpec {
    private String name;
    // source is a supplier to allow it to be deferred
    private Supplier<DataSourceSpec> source;

    private int partitionCount = 5;

    private CrossfoldMethod method = CrossfoldMethod.PARTITION_USERS;
    private PartitionMethodSpec userPartitionMethod;
    private Integer sampleSize;

    private boolean includeTimestamps = true;

    private OutputFormat outputFormat = OutputFormat.CSV;
    private Path outputDir;

    public CrossfoldSpec() {
        PartitionMethodSpec.Holdout hold = new PartitionMethodSpec.Holdout();
        hold.setOrder("random");
        hold.setCount(10);
        userPartitionMethod = hold;
        source = Suppliers.ofInstance(null);
    }

    public String getName() {
        if (name == null && source != null) {
            DataSourceSpec ss = source.get();
            if (ss != null) {
                return ss.getName();
            }
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataSourceSpec getSource() {
        return source.get();
    }

    public void setDeferredSource(Supplier<DataSourceSpec> deferredSource) {
        source = deferredSource;
    }

    public void setSource(DataSourceSpec source) {
        setDeferredSource(Suppliers.ofInstance(source));
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public void setPartitionCount(int partitionCount) {
        this.partitionCount = partitionCount;
    }

    public CrossfoldMethod getMethod() {
        return method;
    }

    public void setMethod(CrossfoldMethod method) {
        this.method = method;
    }

    public PartitionMethodSpec getUserPartitionMethod() {
        return userPartitionMethod;
    }

    public void setUserPartitionMethod(PartitionMethodSpec userPartitionMethod) {
        this.userPartitionMethod = userPartitionMethod;
    }

    public boolean getIncludeTimestamps() {
        return includeTimestamps;
    }

    public void setIncludeTimestamps(boolean includeTimestamps) {
        this.includeTimestamps = includeTimestamps;
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Integer getSampleSize() {
        return sampleSize;
    }

    @JsonProperty(required=false)
    public void setSampleSize(Integer sz) {
        sampleSize = sz;
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(Path outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Get the data sets that will be produced by the crossfold specified by this spec.
     * @return A list of data set specifications.
     */
    @JsonIgnore
    public List<DataSetSpec> getDataSets() {
        Preconditions.checkState(outputDir != null, "No output directory specified");
        Preconditions.checkState(source != null, "No data source specified");
        List<DataSetSpec> specs = new ArrayList<>(partitionCount);
        for (int i = 1; i <= partitionCount; i++) {
            DataSetSpec dss = new DataSetSpec();
            dss.setName(getName() + "." + i);
            dss.setTrainSource(makeDataSource(String.format("part%02d.train", i)));
            dss.setTestSource(makeDataSource(String.format("part%02d.test", i)));
            dss.setAttribute("DataSet", getName());
            dss.setAttribute("Partition", i);
            specs.add(dss);
        }
        return specs;
    }

    private DataSourceSpec makeDataSource(String basename) {
        switch (outputFormat) {
        case PACK: {
            PackedDataSourceSpec spec = new PackedDataSourceSpec();
            spec.setDomain(getSource().getDomain());
            spec.setFile(getOutputDir().resolve(basename + ".pack"));
            return spec;
        }
        default: {
            TextDataSourceSpec spec = new TextDataSourceSpec();
            spec.setDomain(getSource().getDomain());
            // the compression mode is encoded in the output format extension
            spec.setFile(getOutputDir().resolve(basename + "." + outputFormat.getExtension()));
            spec.setDelimiter(",");
            return spec;
        }
        }
    }

    /**
     * Get the file that the crossfold will produce containing the output partitions.
     * @return A path to the `all-partitions.json` file that the crossfolder will produce.
     */
    @JsonIgnore
    public Path getPartitionSpecFile() {
        return getOutputDir().resolve("all-partitions.json");
    }
}
