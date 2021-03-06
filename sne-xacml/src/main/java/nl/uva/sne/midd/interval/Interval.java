/*
 * Copyright (C) 2013-2016 Canh Ngo <canhnt@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA
 */

package nl.uva.sne.midd.interval;

import nl.uva.sne.midd.MIDDException;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author Canh Ngo
 */
public class Interval<T extends Comparable<T>> {

    private EndPoint<T> lowerBound;
    private EndPoint<T> upperBound;

    private boolean lowerBoundClosed = false;
    private boolean upperBoundClosed = false;

    /**
     * Interval with a single end point
     *
     * @param bound
     */
    public Interval(EndPoint<T> bound) {
        this.lowerBound = this.upperBound = bound;
        this.lowerBoundClosed = (this.upperBoundClosed = true);
    }

    /**
     * Create an interval either be (-inf, bound) or (bound, +inf)
     *
     * @param inf
     * @param v
     */
    public Interval(EndPoint.Infinity inf, final T v) throws MIDDException {
        if (inf == EndPoint.Infinity.NEGATIVE) {
            this.lowerBound = EndPoint.of(EndPoint.Infinity.NEGATIVE);
            this.upperBound = EndPoint.of(v);
        } else {
            this.lowerBound = EndPoint.of(v);
            this.upperBound = EndPoint.of(EndPoint.Infinity.POSITIVE);
        }
    }

    public Interval(EndPoint<T> lowerBound, EndPoint<T> upperBound) throws MIDDException {
        this(lowerBound, upperBound, false, false);
    }

    @SuppressWarnings("unchecked")
    public Interval(final EndPoint<T> lowerBound, final EndPoint<T> upperBound, boolean isLowerBoundClosed, boolean isUpperBoundClosed) throws MIDDException {
        this.lowerBound = new EndPoint(lowerBound);
        this.lowerBoundClosed = isLowerBoundClosed;

        this.upperBound = new EndPoint(upperBound);
        this.upperBoundClosed = isUpperBoundClosed;
    }

    /**
     * Copy constructor for the immutable class
     *
     * @param interval
     * @throws MIDDException
     */
    public Interval(final Interval<T> interval) throws MIDDException {
        this(interval.lowerBound, interval.upperBound, interval.lowerBoundClosed, interval.upperBoundClosed);
    }

    /**
     * Create an interval containing only an end-point, i.e., [v]
     * @param bound
     * @throws MIDDException
     */
    public Interval(T bound) throws MIDDException {
        this.lowerBound = this.upperBound = new EndPoint<>(bound);
        this.lowerBoundClosed = this.upperBoundClosed = true;
    }

    /**
     * Create an interval with open end points
     * @param lowerBound
     * @param upperBound
     * @throws MIDDException
     */
    public Interval(T lowerBound, T upperBound) throws MIDDException {
        this(lowerBound, upperBound, false, false);
    }

    public Interval(T lowerBound, T upperBound, boolean isLowerBoundClosed, boolean isUpperBoundClosed) throws MIDDException {
        this.lowerBound = new EndPoint<>(lowerBound);
        this.upperBound = new EndPoint<>(upperBound);

        this.lowerBoundClosed = isLowerBoundClosed;
        this.upperBoundClosed = isUpperBoundClosed;
    }

    /**
     * Return the complement section of the interval.
     *
     * @param op
     * @return The complemented interval(s), or an empty list if the complement is empty.
     */
    public List<Interval<T>> complement(final Interval<T> op) throws MIDDException {

        final boolean disJoined = (this.lowerBound.compareTo(op.upperBound) >= 0) ||
                (this.upperBound.compareTo(op.lowerBound) <= 0);

        if (disJoined) {
            Interval<T> newInterval = new Interval<>(this.lowerBound, this.upperBound);

            final boolean isLowerClosed;
            if (this.lowerBound.compareTo(op.upperBound) == 0) {
                isLowerClosed = this.lowerBoundClosed && !op.upperBoundClosed;
            } else {
                isLowerClosed = this.lowerBoundClosed;
            }
            newInterval.closeLeft(isLowerClosed);

            final boolean isUpperClosed;
            if (this.upperBound.compareTo(op.lowerBound) == 0) {
                isUpperClosed = this.upperBoundClosed && !op.upperBoundClosed;
            } else {
                isUpperClosed = this.upperBoundClosed;
            }
            newInterval.closeRight(isUpperClosed);

            // return empty if new interval is invalid
            if (!newInterval.validate()) {
                return ImmutableList.of();
            }
            return ImmutableList.of(newInterval);
        } else {
            final Interval<T> interval1 = new Interval<>(this.lowerBound, op.lowerBound);
            final Interval<T> interval2 = new Interval<>(op.upperBound, this.upperBound);

            interval1.closeLeft(!interval1.isLowerInfinite() && this.lowerBoundClosed);
            interval1.closeRight(!interval1.isUpperInfinite() && !op.lowerBoundClosed);

            interval2.closeLeft(!interval2.isLowerInfinite() && !op.upperBoundClosed);
            interval2.closeRight(!interval2.isUpperInfinite() && this.upperBoundClosed);

            final List<Interval<T>> result = new ArrayList<>();
            if (interval1.validate()) {
                result.add(interval1);
            }
            if (interval2.validate()) {
                result.add(interval2);
            }
            return ImmutableList.copyOf(result);
        }
    }

    /**
     * Return true if interval in  the argument is the subset of the current interval.
     *
     * @param i
     * @return
     */
    public boolean contains(final Interval<T> i) {

        int compareLow, compareUp;
        compareLow = this.lowerBound.compareTo(i.lowerBound);
        compareUp = this.upperBound.compareTo(i.upperBound);

        if (compareLow < 0) {
            // check the upper bound
            if (compareUp > 0) {
                return true;
            } else if ((compareUp == 0) &&
                    (this.upperBoundClosed || !i.upperBoundClosed)) {
                return true;
            }
        } else if (compareLow == 0) {
            if (this.lowerBoundClosed || !i.lowerBoundClosed) { // lowerbound satisfied
                {
                    // check upperbound
                    if (compareUp > 0) {
                        return true;
                    } else if ((compareUp == 0) &&
                            (this.upperBoundClosed || !i.upperBoundClosed)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) ||
            (getClass() != obj.getClass())) {
            return false;
        }

        if (obj instanceof Interval) {
            return equals((Interval)obj);
        }
        return false;

    }

    private boolean equals(Interval<T> other) {
        return lowerBound.equals(other.lowerBound)
            && (lowerBoundClosed == other.lowerBoundClosed)
            && (upperBound.equals(other.upperBound))
            && (upperBoundClosed == other.upperBoundClosed);

    }

    /**
     * Return the immutable endpoint of the interval
     *
     * @return
     */
    public EndPoint<T> getLowerBound() throws MIDDException {
        return new EndPoint<>(this.lowerBound);
    }

    /**
     * Return the immutable endpoint of the interval.
     *
     * @return
     */
    public EndPoint<T> getUpperBound() throws MIDDException {
        return new EndPoint<>(this.upperBound);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((lowerBound == null) ? 0 : lowerBound.hashCode());
        result = prime * result + (lowerBoundClosed ? 1231 : 1237);
        result = prime * result
                + ((upperBound == null) ? 0 : upperBound.hashCode());
        result = prime * result + (upperBoundClosed ? 1231 : 1237);
        return result;
    }

    /**
     * Check if the value is presenting in the interval.
     *
     * @param value
     * @return
     */
    public boolean hasValue(final T value) throws MIDDException {

        //special processing when missing attribute
        if (value == null) {
            return this.isLowerInfinite() || this.isUpperInfinite();
        }


        EndPoint<T> epValue = new EndPoint<>(value);

        int compareLow = this.lowerBound.compareTo(epValue);
        int compareUp = this.upperBound.compareTo(epValue);

        if ((compareLow < 0 || (compareLow == 0 && this.lowerBoundClosed)) &&
                (compareUp > 0 || (compareUp == 0 && this.upperBoundClosed))) {
            return true;
        }

        return false;
    }

    /**
     * Combine two interval, check if the bounds should be included Bug: not count use-cases when bounds are infinities.
     * @param target
     * @return
     */
    public Interval<T> includeBound(final Interval<T> target) {

        if (target.lowerBound.equals(target.upperBound)) { // target is a single-value interval
            if (!this.lowerBound.equals(this.upperBound)) {
                if (this.upperBound.equals(target.upperBound)) {
                    this.upperBoundClosed = true;
                    return this;
                } else if (this.lowerBound.equals(target.upperBound)) {
                    this.lowerBoundClosed = true;
                    return this;
                } else {
                    throw new RuntimeException("Error! Cannot combine two separated interval");
                }
            } else {
                throw new RuntimeException("Error! Only support combine single value interval");
            }
        } else if (this.lowerBound.equals(this.upperBound)) { // (*this) is the single-value interval
            if (target.lowerBound.equals(target.upperBound)) {
                throw new RuntimeException("Error! Only support combine single value interval");
            }

            if (target.lowerBound.equals(this.lowerBound)) {
                this.lowerBound = target.lowerBound;
                this.upperBound = target.upperBound;
                this.lowerBoundClosed = true;
                this.upperBoundClosed = target.upperBoundClosed;

                return this;
            } else if (target.upperBound.equals(this.lowerBound)) {
                this.lowerBound = target.lowerBound;
                this.upperBound = target.upperBound;
                this.lowerBoundClosed = target.lowerBoundClosed;
                this.upperBoundClosed = true;

                return this;
            } else {
                throw new RuntimeException("Error! Cannot combine two separated interval");
            }
        } else {
            throw new RuntimeException("Error! Only support combine single value interval");
        }
    }

    public boolean isIntersec(final Interval<T> interval) {
        int c = this.upperBound.compareTo(interval.lowerBound);
        if (c < 0) {
            return false;
        } else if (c == 0) {
            return this.upperBoundClosed && interval.lowerBoundClosed;
        }
        c = this.lowerBound.compareTo(interval.upperBound);
        if (c > 0) {
            return false;
        } else if (c == 0) {
            return this.lowerBoundClosed && interval.upperBoundClosed;
        }
        return true;
    }

    public boolean isLowerBoundClosed() {
        return this.lowerBoundClosed;
    }

    public boolean isLowerInfinite() {
        return lowerBound.isInfinity();
    }

    public boolean isUpperBoundClosed() {
        return this.upperBoundClosed;
    }

    public boolean isUpperInfinite() {
        return upperBound.isInfinity();
    }

    public void setLowerBound(final EndPoint<T> lowerBound) throws MIDDException {
        this.lowerBound = new EndPoint<>(lowerBound)  ;
    }

    public void setLowerBound(final T value) throws MIDDException {
        this.lowerBound = new EndPoint<>(value);
    }

    public Interval<T> closeLeft(boolean b) {
        if (b && lowerBound.isInfinity()) {
            throw new IllegalArgumentException("Cannot set closed because the lower bound is infinite");
        }
        this.lowerBoundClosed = b;
        return this;
    }

    public Interval<T> closeRight(boolean b) {
        if (b && upperBound.isInfinity()) {
            throw new IllegalArgumentException("Cannot set closed because the upper bound is infinite");
        }
        this.upperBoundClosed = b;
        return this;
    }

    public void setUpperBound(final EndPoint<T> upperBound) throws MIDDException {
        this.upperBound = new EndPoint<>(upperBound);
    }

    public void setUpperInfinite() {
        this.upperBound.setInfinity(EndPoint.Infinity.POSITIVE);
        this.upperBoundClosed = false;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        if (isSinglePoint()) {
            builder.append("[").append(lowerBound).append("]");
        } else {
            builder.append((this.lowerBound.negativeInfinity() || !this.lowerBoundClosed) ? "(" : "[")
                    .append(this.lowerBound)
                    .append(", ")
                    .append(this.upperBound)
                    .append((this.upperBound.positiveInfinity() || !this.upperBoundClosed) ? ")" : "]");

        }
        return builder.toString();
    }

    /**
     * <code>True</code> if the inteval only contains a single point value
     * @return
     */
    public boolean isSinglePoint() {
        return lowerBoundClosed && upperBoundClosed && lowerBound.equals(this.upperBound);
    }

    /**
     * Check if the interval is valid or an empty interval.
     *
     * @return
     */
    public boolean validate() {
        int compareBound = this.lowerBound.compareTo(this.upperBound);

        return (compareBound < 0) ||
                (compareBound == 0 && this.lowerBoundClosed && this.upperBoundClosed);
    }

    /**
     * The interval contains only 1 value: upper==lower
     *
     * @param value
     */
    public void setSingleValue(final EndPoint<T> value) throws MIDDException {
        this.upperBound = this.lowerBound = new EndPoint<>(value);
        this.lowerBoundClosed = this.upperBoundClosed = true;

    }

    public Class<T> getType() throws MIDDException {

        if (this.lowerBound.getType() != null) {
            return this.lowerBound.getType();
        }

        if (this.upperBound.getType() != null) {
            return this.upperBound.getType();
        }

        throw new MIDDException("Unsupported (-inf, +inf) interval");
    }

    public static <T extends Comparable<T>> Interval<T> of(T lowerBound, T upperBound, boolean isLowerBoundClosed, boolean isUpperBoundClosed) throws MIDDException {
        return new Interval(lowerBound, upperBound, isLowerBoundClosed, isUpperBoundClosed);
    }

    public static <T extends Comparable<T>> Interval<T> of(T lowerBound, T upperBound) throws MIDDException {
        return new Interval(lowerBound, upperBound);
    }

    /**
     * Create an interval either be (-inf, v) or (v, +inf)
     * @param v
     * @param inf
     * @param <T>
     * @return
     * @throws MIDDException
     */
    public static <T extends Comparable<T>> Interval<T> from(final EndPoint.Infinity inf, final T v) throws MIDDException {
        return new Interval<>(inf, v);
    }

    /**
     * Create an interval with a single endpoint
     * @param v
     * @param <T>
     * @return
     * @throws MIDDException
     */
    public static <T extends Comparable<T>> Interval<T> of(final T v) throws MIDDException {
        return new Interval(v);
    }
}
