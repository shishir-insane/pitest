/*
 * Copyright 2011 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.report;

import java.io.IOException;
import java.io.Writer;

import org.pitest.Description;
import org.pitest.TestResult;
import org.pitest.extension.TestListener;
import org.pitest.functional.Option;
import org.pitest.mutationtest.CoverageDatabase;
import org.pitest.mutationtest.instrument.MutationMetaData;
import org.pitest.mutationtest.results.MutationResult;
import org.pitest.util.Unchecked;

public class CSVReportListener implements TestListener {

  private final Writer out;

  public CSVReportListener(final CoverageDatabase coverage,
      final long startTime, final ResultOutputStrategy outputStrategy,
      final SourceLocator... locators) {
    this(outputStrategy.createWriterForFile("mutations.csv"));

  }

  public CSVReportListener(final Writer out) {
    this.out = out;
  }

  public void onRunStart() {

  }

  public void onTestStart(final Description d) {
  }

  public void onTestFailure(final TestResult tr) {
    writeResult(tr);
  }

  public void onTestError(final TestResult tr) {
    writeResult(tr);
  }

  public void onTestSkipped(final TestResult tr) {
  }

  public void onTestSuccess(final TestResult tr) {
    writeResult(tr);
  }

  public void onRunEnd() {
    try {
      this.out.close();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  private void writeResult(final TestResult tr) {
    try {
      for (final MutationMetaData metaData : extractMetaData(tr)) {
        for (final MutationResult mutation : metaData.getMutations()) {
          this.out.write(makeCsv(mutation.getDetails().getFilename(), mutation
              .getDetails().getClazz(), mutation.getDetails().getMethod(),
              mutation.getDetails().getLineNumber(), mutation.getStatus(),
              createKillingTestDesc(mutation.getKillingTest()))
              + System.getProperty("line.separator"));
        }
      }
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  private String createKillingTestDesc(final Option<String> killingTest) {
    if (killingTest.hasSome()) {
      return killingTest.value();
    } else {
      return "none";
    }
  }

  private String makeCsv(final Object... os) {
    final StringBuffer sb = new StringBuffer();
    for (int i = 0; i != os.length; i++) {
      sb.append(os[i].toString());
      if (i != os.length - 1) {
        sb.append(",");
      }
    }
    return sb.toString();
  }

  private Option<MutationMetaData> extractMetaData(final TestResult tr) {
    return tr.getValue(MutationMetaData.class);

  }

}