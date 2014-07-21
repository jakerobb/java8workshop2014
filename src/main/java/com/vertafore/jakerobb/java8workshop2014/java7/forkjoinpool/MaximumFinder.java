/*
 * Copyright 2014 Vertafore, Inc. All rights reserved.
 *
 * Disclaimers:
 * This software is provided "as is," without warranty of any kind, express
 * or implied, including but not limited to the warranties of merchantability,
 * fitness for a particular purpose and non-infringement. This source code
 * should not be relied upon as the sole basis for solving a problem whose
 * incorrect solution could result in injury to person or property. In no
 * event shall the author or contributors be held liable for any damages
 * arising in any way from the use of this software. The entire risk as to
 * the results and performance of this source code is assumed by the user.
 *
 * Permission is granted to use this software for internal use only, subject
 * to the following restrictions:
 * 1. This source code MUST retain the above copyright notice, disclaimer,
 * and this list of conditions.
 * 2. This source code may be used ONLY within the scope of the original
 * agreement under which this source code was provided and may not be
 * distributed to any third party without the express written consent of
 * Vertafore.
 * 3. This source code along with all obligations and rights under the
 * original License Agreement may not be assigned to any third party
 * without the expressed written consent of Vertafore, except that 
 * assignment may be made to a successor to the business or 
 * substantially all of its assets. All parties bind their successors,
 * executors, administrators, and assignees to all covenants of this
 * Agreement.
 *
 * All advertising materials mentioning features or use of this software
 * must display the following acknowledgment:
 * Trademark Disclaimer:
 * All patent, copyright, trademark and other intellectual property
 * rights included in the source code are owned exclusively by Vertafore,
 * Inc.
 */
package com.vertafore.jakerobb.java8workshop2014.java7.forkjoinpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class MaximumFinder extends RecursiveTask<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(MaximumFinder.class);

    private static final int SEQUENTIAL_THRESHOLD = 5;

    private final Integer[] data;
    private final int start;
    private final int end;

    public MaximumFinder(Integer[] data, int start, int end) {
        this.data = data;
        this.start = start;
        this.end = end;
    }

    public MaximumFinder(Integer[] data) {
        this(data, 0, data.length);
    }

    @Override
    protected Integer compute() {
        final int length = end - start;
        if (length < SEQUENTIAL_THRESHOLD) {
            return computeDirectly();
        }
        final int split = length / 2;
        final MaximumFinder left = new MaximumFinder(data, start, start + split);
        left.fork();
        final MaximumFinder right = new MaximumFinder(data, start + split, end);
        return Math.max(right.compute(), left.join());
    }

    private Integer computeDirectly() {
        LOG.info(String.format("%s computing: %d to %d", Thread.currentThread(), start, end));
        int max = Integer.MIN_VALUE;
        for (int i = start; i < end; i++) {
            if (data[i] > max) {
                max = data[i];
            }
        }
        return max;
    }

    public static void main(String[] args) {
        // create a random data set
        final Integer[] data = new Integer[1000];
        final Random random = new SecureRandom();
        for (int i = 0; i < data.length; i++) {
            data[i] = random.nextInt(100);
        }

        // create a thread pool
        final ForkJoinPool pool = new ForkJoinPool(4);

        // find the maximum
        Integer maximum = pool.invoke(new MaximumFinder(data));
        LOG.info(maximum.toString());
    }
}

//        Integer[] data = pool.invoke(new RandomDataGenerator(1000));
