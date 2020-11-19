package com.atomatus.linq;

/**
 * Generated result group from collection, set or array interation or filter using {@link CollectionHelper}
 * or another {@link IterableResult}.
 *
 * @param <K> iterable element key type
 * @param <V> iterable element value type
 * @author Carlos Matos
 */
public abstract class IterableResultGroup<K, V> extends IterableResultMap<K, IterableResult<V>> {

    interface IteratorGroupCalculator<K, V> {
        IterableResultMap<K, Integer> size();

        <N extends Number> IterableResultMap<K, N> sum(Class<N> resultClass);

        <N extends Number> IterableResultMap<K, N> sum(CollectionHelper.FunctionMount<V, N> mountFun);

        <N extends Number> IterableResultMap<K, N> average(Class<N> resultClass);

        <N extends Number> IterableResultMap<K, N> average(CollectionHelper.FunctionMount<V, N> mountFun);

        <N extends Number> IterableResultMap<K, N> mean(Class<N> resultClass);

        <N extends Number> IterableResultMap<K, N> mean(CollectionHelper.FunctionMount<V, N> mountFun);

        IterableResultMap<K, V> min();

        <C extends Comparable<C>> IterableResultMap<K, V> min(CollectionHelper.FunctionMount<V, C> mountFun);

        IterableResultMap<K, V> max();

        <C extends Comparable<C>> IterableResultMap<K, V> max(CollectionHelper.FunctionMount<V, C> mountFun);

        IterableResultGroup<K, V> sample(CollectionHelper.CompareEntryValid<K> checkFun);

        IterableResultGroup<K, V> amount(int count);
    }

    protected interface IteratorGroup<K, V> extends IteratorGroupCalculator<K, V>, IterableResultMap.IteratorMap<K, IterableResult<V>> {

        IterableResult<K> keySet();

        IterableResult<IterableResult<V>> values();
    }

    protected abstract IteratorGroup<K, V> initIterator();

    protected IteratorGroup<K, V> getIteratorAsGroup() {
        return (IteratorGroup<K, V>) super.getIterator();
    }

    //region IteratorGroup Actions

    /**
     * Generate an iterable result with the amount of items in each entry.
     *
     * @return iterable result map within amount of items size in each entry.
     */
    public final IterableResultMap<K, Integer> size() {
        return getIteratorAsGroup().size();
    }

    /**
     * Apply summation operation in a sequence of any kind of number.
     *
     * @param resultClass number type class.
     * @param <N>         number type
     * @return summation result
     */
    public final <N extends Number> IterableResultMap<K, N> sum(Class<N> resultClass) {
        return getIteratorAsGroup().sum(resultClass);
    }

    /**
     * Apply summation operation in a sequence of any kind of number.
     *
     * @param mountFun function to get a target number in element.
     * @param <N>      number type
     * @return summation result
     */
    public final <N extends Number> IterableResultMap<K, N> sum(CollectionHelper.FunctionMount<V, N> mountFun) {
        return getIteratorAsGroup().sum(mountFun);
    }

    /**
     * <p>
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     * </p>
     * <img  src="data:image/png;base64,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" />
     * @param resultClass result number class type
     * @param <N>         result number type
     * @return result number
     */
    public final <N extends Number> IterableResultMap<K, N> average(Class<N> resultClass) {
        return getIteratorAsGroup().average(resultClass);
    }

    /**
     * <p>
     * Average is defined as the sum of all the values divided by the total number of values in a given set.
     * </p>
     * <img  src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAWMAAABlCAYAAACY7IJIAAAACXBIWXMAAA7EAAAOxAGVKw4bAAATgElEQVR4nO3da1hU5doH8H/6ttsMDmU7RHKuy2ADEQQeOaWoI4giCqhocooUUd8Okplp5i5BPOUB0CIVUXeCqJwpRQU8pMaM4lWADgRv4yaHbEBLHWfGNtp6P5CTI6YMs4a1Bu7fJ1mwnnWD8J81z3oOTzAMw4AQQginenFdACGEEApjQgjhBQpjQgjhAQpjQgjhAQpjQgjhAQpjQgjhAQpjQgjhAQpjQgjhAQpjQgjhgf/hugBCCAEAjVqDstJS/M78DltbWxQXFWFmeDjc3N25Lq1LUBgTQnhBYCnApUuXAABOL74Ii79bQKPRcFxV16FuCkIIbxwrK4OdnR1cXFxwvLwcbm49464YoDAmhPCEXC5Hr969ERwaAqlEgkFDBkNgKeC6rC5DYUwI4YWTx08gKjoaAJCfkwut9jaUSiXHVXWdJ2gJTUIIHygUCohEIt2/n376aQiFQo6r6joUxoQQwgPUTUEI6TJSiaRHdT0Ygoa2EUK6jOyiDK9Hv2ZUG5s/+xTjAgJYqog/KIwJIV1mVuxsVJ47h2Pl5bpj0TExsLN74ZHnNTe3oKggH1eu/AwrKyuT1sgV6jMmhHQpjVqDMb6+UKlUAAAnJydkHzjw2GFscrkcQeMnYPeeL+Dl7d0VpXYp6jMmhHQpgaUAW9I+031cX1+P9O3bH3uevb09goODIbsoM2V5nKE7Y0IIJ4oLi7Bk8WIAwN8tLLB1+7bH3vHK5XJckl+Cn7/fY9tXKpVofuBh4b11Lh78nIVAAAcHB0O/BVbRnTEhhBPBoSGYGjYNAHBbq8Xbb7wJhULxyHPs7e07FMQA0NraiqKCQsyYFoblHyzTW+eitbUVWzZvxoxpYSgqKESvXtxHId0ZE0I4o9VqMWVyMBobGwEAAwcORPb+/ej7j2dZu8bYUaPw9DN9UVBcpDumUWuweNEiLHp/Mezt7Vm7ljG4fzkghPRYFhYW2LF7FwSCtod3jY2NWJmYwOo1fEaMQF1tre6BIQAkfPQRr4IYoDAmhHBMJBLhvfcX6z4uOVSC0qNHWWvfy6utH/ro4SMAgKSERMTEzuZVEAPUTUEI4Yl34+NRcqgEACAQCLB7zxesLCzf1NQE/zFi+I4ehcGDB2PCxIm8C2KAwpgQwiMhQZNQX18PABAKhThx6hQry2jea/dAXi5vdw6hbgpCCG98tm2rrv9YpVJ1aPxxd0FhTAjhDZFIhH+t+BhA28iKsBnTjW5z4/oNmBkRDgA49fXXRrdnKhTGhBBeOXTwIIRCIXIK8jFgwACj2lqy6D0MHjIE4ZGREAqF+O6771iqkn0UxoQQ3sjLycX5c5XYnbnH6IXllyx6D57e3rpJImKxGLWyWjbKNAkKY0IIL6Qmp2D5smVYvW4tXFxcjGorKXElAgIDMW16mO7YCF9fXG1pgVQiMbZUk6AwJoRwrrysHFvT0pC0ejXGT5jQ6XYUCgWiwiOg1WraTZu26W8DANiSullvAsj9577k6NTpaxuLhrYRQjglk8kQHR4Bf39/rNu4odPtlJeVY1dGhu7jt+MX6BYeysvJRVlpqS6EhUIhFi9dwqvxxhTGhBDONDU1ITbmdbi+/DI2piR3+LzsrCxYWvZBcGgIK3VotVrszcyEQCBAeGQkK20airopCCGciY15HU899RRWrlpl0HkH9h+A2G8sa3VYWFigvu57DBoyhLU2DdXjtl2qqa7GrowMXLr0H9jb22PGzFd1b2VqqquxNe1z/PTTTxgjFmOs31ijZ+s0NTUZPTzn65MnsX3rNqjVari4uiA2Lk739qq8rBz792XjluoW/Pz9IPbzY+Wtl1arxZUrV4xqS6vVojA/Hwe/Ogi1Wo1JkydhUnAwbGza+u6ys7JwYP8BWFpaImhSEEJCpxg920qj1kB1S6W7Bt/I5XL0t+lv1PdZU12NL4u/xLmzZ2Hdzxqxc+bAfdAgWFhYQC6XIyM9HbKLMri4uiAkdAo8vTxZ/A7YoVFr8E78Avz3v78hv6iowz8PjVqDVUkrcfv2baNHW9xPq9WivqHB6AeHxuhxYezm7o53Fy/GOPFYuLi66C1m7ebujhs3bmDpsg9Y29Yl4eOPsX3HDqPaGDV6NFqaW7B82TJMmjxJLyA9vTyxOjERKZ9uYXWa5/81NGDdmrXIzN7b6TYsLCwQHhmJ5uYWbE1Lw6q1a/RC0tKyDywtLfH59m2s/WGdOXMa0goJln/8ESvtse2jD5fr9WV2hpu7O16ws8OYkb7o37+/Xlv29vZoaWlBVHS03kgCvpk7Zw7OV1Zi4MCBmDtnTofOudrSoltqM3BiIKv1VFdVYdiwYVCpVKyGvCF6XBgDbbN8nJyccP5cpd7xpIREzIqNZXV/LWtra1bamTY9DOvWrMHFCxf0jietSMCWrZ9z+or+ONOmh2FrWhqkFRJdnSqVChcuXDAq7B/GysoKwm66YeX9hEIhhnkMx/lK/d/hvJxciMViXgdxanKKru7GxkZdwBpi4At2rNYkqZBAdfMmbty4wVkY99g+4+DQEDQ2NkL5x9YrhQUF8BkxosO7CHBhpO9ISL6p0H2c9umnmPfmG7wOYqDtxc/Wtj/Ky8oAtHXdLFq4EDGzXue2MDPn7eMDlUqlGzebmpwChULB2QOojsjOysLWtDSj2xFasRuYc+fPw7qNGyASiVht1xA98s4YAMYHBmLDJ+tx+utTAIBn+vbldRADQGBQEEoOlUAmkyE/JxcR0VG8GprzKD4jRiA/Nw8KhQKJK1ZgY3IyZ3cg3YWHhwcAoPTIUUgqJBCJRLy+IwaAQUOGoLahvtPnq1QqyC5ehHW/fixW1dalxrUee2csEonwnLU10rZsAQDeBzHQdicEAG/P/19MnR5mNkEM/LnAd0xEJJYuW0ZBzAI3d3cIhUIUF7VtJ8T3IAZg9Ls4oVAIL29vs/rd76gee2cMAE6Ojvjmm28QOHEiK+1NCW4/5lF55QqmXGx//IMPPzT4KbdQKIStbX88+be/sdo1UV5Wjk83b9Y7dvv2bTQrle2+p2f79kXGv3cbfI3+trYAgEFDBrP2h5SanIITx4/rHdNqNFBrNO2Ou7i6YNWaNaxct6PmzpmDluYWvWM/Njbi4+X/goVAf/TAWwsWdOqGYIBIhLraWlZWNyMcY3qo3AM5zHg/f8bZwZE5euSIya4TFxvLWlspm5J1NV++fJm1dh+muqqKiZwZzlp7CxcsYDyGDGU8hgxlrc2HOXrkCLNyRYJJr2GMyJnhjKSigpW2igoKmeCJQYyzgyOzNzOTlTYJd3pkN0VeTi7OSiRYv2kjAEBaYbqFQ9gaTZGXkwuRSIQ33noLgGlrZtuSRe/h1fBwiMViqFQqyOVyk12rp4ymkMlkOHH8GDL3ZQMALl36D7cFdbF7D967kx4XxtlZWVAoFFi3cQPc3N3xnLU1Tp86xXVZj1RYUIBn+vbFtOlh8PX1BQCcOc3vmoG2gfTvxscjIDAQXt7eCAhsGxv6YBcCMUx5WTl2bNuGVWvXtg1xGz6c14umP+jrkydRXFjU6fOLC4swP24uixXxA2t9xkkJiairq3vk1wx4/nmjFgIxVl5OLpqbWxC/8B3dsVd8fFBcXAy5XM7LhwJ5Obl6Iz36/uNZODk5oeRQCZJWr2FlfzBT+XDpUsyZN0/Xv+3n7wehUAhJRQVmx8ZyXJ15Ki8rx9GSEmxKTdUd8/P3xydr10KhUHA6NKujhg/3wN3f73b6/ODQEOzauZPFiviBtTCeOj0Mqps32WqOdXk5uSjIz283ycDT2xvFxcX4sqhYL6SBtl/81YmJKDz4FSdP/zPS01Ff9327FzAPT0/U19ejoqKi3UMfmUyG1SuTUP/99wgOCeFkJppKpULSigSMEY9t96DR6cUXcf5c5UNnOqUmp+Dc2bNQq9X4Iiuzy37mMpkM7y6Ix6L3F2NcQIDuuFKpxKJ3FsLZ2bndzzE1OQXFBQVI372ry17EpRIJ9u/Lbjej08vnz5XJHvwd5huFQoG8nFxMmx6m9/+bkZ6O48fav2Pq189a74WnW+O609rUykrLmNmvxTDODo5M6ORgprqqSvc5qUTKRM4MZ5wdHJnBbu5MyqZkRqFQ6J0/0ucVo65/uKTE4HP2ZmYycbGxjLODIxMXG6tXU0F+vu4hntjXl9mxfTujvqXWfb6ooJBhGIZR31IzoZODGalE2qm6f7l6jSkrLTP4vM+2bGFG+rzCODs4Mhs+Wa/X3o7t25mh7oN0/xcnT5zQfb6hoUH3fSxburTTD7l++OEHg8/dm5nJODs4MimbkvWONzQ0MM4OjsxQ90HtzgmdHMw4Ozga/P9bVlrG/PzzzwadI5VIdb8PD/4Ol5WWMQsXLNDVuWplEtPQ0GBQ+13tjXnzjW4jdHIwC5XwC2tLaEolEuzeuUt3xzM1bJruLkOhUGDT+vVobm7BgOefR0BgoFmM6wWAqPAI1qfsdpWkhESMGx/A6vTurpCRno7YuLguvWZNdfVD1/aQyWSwsrJq9/ZfpVLh8uXLvJ/9qFQqUV1VBSsrK7xgZ4d9e7MRN3cuZ91bKpUKa1evbjfMsLysHBdqatp9fZ8+lg/9XZgSHIKC4s73O/MRa90UXt7eeMHODhPGBWBi0ES9t3sikQi//fZfiMeKER4Ryet+zu5Co9bgl1+umVUQa9QaZO/NQm5OLmurz3XUXy2y9FdhKxQKeR/EAGBjY4OSgwcxRjwW169fx4njxxE0eRIcHBw4qeer4mKMGj263XE/fz+zuUEzFVZHU9jY2MDV1RWSM9/oHc/LycXUsDDExsVREHeR/fv2ISEpiesyDCKwFCA2Lg6vxbyG9WvXcV1Ot3H61GlYCCwwfsIE/H73LmdBDAAHvzqIutpHP+h/HJlMBq1GA4VCwVJV/MD60DYPT0/8dOUKZDIZgLYgBsxjunF3kZGeDi8fb7OdcvyymxuszLR2vpHL5bC1tcW4gABIJRJ4eHK7tvHa9Z8Y/ZDRxcUFh8tKzWLkiCFYnw49ZGjbSvmlR46i6ttvDd4apbiwCGr1rQ59rZePj8nfyl5taYFGrTGbO/riwiLI5XIIBAJUnDnD6TYyhqiprkZzcwv8/P1woaYG8958g+uSuoUTx4/r+lxPnzoNoZUVtFpthxfGyc7K6vC1xvr7P3ZR/+4WoGxiPYzv9Qdl7dmDyOhoxC80LAiuXbuGmx0cInf79m2D6zOEXC5HQtJK1NRUm03fq01/GwSH/Pni94Idu+u+mko/GxscKz+G67/+2qE/atIxY8Ri3Q3LSN+RGCASGbRCWfMDa2s8Smtrq8H1kT+ZZEPSqPAInK+sxInTp+iPihBCOoD1O+P739Z89+23GD9hAtuX6JCIV2dCq9Vycm1CzEVnV+EzhFKpNNvpy51dTa8zWL0zzsvJhUKhwOw5sfAcOgzRMTFYtvxDg9rwGzUaP1250qGvTVq9+i/XcK2proZGozHo2oT0NAKB4JF7J77k6NThtnbv+eIvu/Pu7UZiblxcXbvsQThrYXxvJbR7U3dDgiahV+/e3W5gNiGEmAIrQ9vuXwntnrH+/qirre2WS90RQgjbjA7jXRk7ce7s2XZjB73/WLxk395sYy9BCCHdXqcf4BUXFuHA/v04X1mJgQMHQiqR6PqLSo8eRX5uHgBga1oaVDdvImRK6CP7pgghpCczydA2QgghhulxO30QQggfURgTQggPUBgTQggPUBgTQggPUBiTLvHrtV+gUf85I1Iul+PXa79wWBHpDu6fx6DValFTXc1hNcahMCYmlZqcgpCgSXjF2xt3f78LlUqFqPAIBI2fgNmzZnFdHjFDKpUKqckpeMnRCRHTZwBoW3A+wM8fM6aFISkhkeMKO4fCmJhU/MJ34OHpiWHDhwMA3l/0HpZ8sBSRUVHQ0tohpBOEQqFukpnYzw9yuRz/ztiJ3IJ8AEBTUxOX5XUahTExudOnTsH5pZewJXUzNiYnw83dHXV1dfgnh9v/EPN2rzvCfdAgHD50COs2bsCdO3cAAKNGj+KytE6jMCYmJZPJ0NjYiB9/bMTb8QsgsBRAqVTifGUlZ8urEvN3VioFAFRXVeH1WbMBAIcPHQIATAoO5qwuY1AYE5OSVrQtnejl5aVbilB3zMc8dk8h/CP9I4ynTg/TbYkmlUoxRiw2270fKYyJSRUXFeE5a2tEREXpjh05fBjDhg+nXWBIp12oqkZ0TAxcXFwAABq1BufOnkPgxIkcV9Z5FMbEZJRKJepqa/HOwoV6+65JKiowZepUlJeVc1gdMVdSiQS/Xr+Ot+MX6I6VHDqEXk88AbHfWLNdyJ7CmJjMhZoLAPS7I6QSCTQaDWplMtjZm8dmqYRfSo8cxbDhw/W6I85KJLC2tsbOHRlwcXXlsLrO671ixYoVXBdBuqerLVfh4+MDD09P3bEnn3wSra13EBEdpdu1mBBDtN65g1GjR2OAaIDuWK/evWFpaYl58+ejT58+HFbXebSEJiGE8AB1UxBCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA9QGBNCCA/8P9QKpGcLKd6nAAAAAElFTkSuQmCC" />
     * @param mountFun function to get target number
     * @param <N>      result number type
     * @return result number
     */
    public final <N extends Number> IterableResultMap<K, N> average(CollectionHelper.FunctionMount<V, N> mountFun) {
        return getIteratorAsGroup().average(mountFun);
    }

    /**
     * <p>
     * A mean is a mathematical term, that describes the average of a sample.<br/>
     * In Statistics, the definition of the mean is similar to the average.<br/>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     * </p>
     * <p>
     * In Statistics, the total amplitude At of a set of values
     * is the difference between the highest and lowest value of the sample, as shown below:
     * </p>
     * <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHMAAAAwCAYAAAAij0UkAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAE1ElEQVR4nO2bL3DiWBzHv725GXCpo+7qwAWXutSlLnUPl3WpAxdkXeqoSx114FJzQ8XNsA5ccoqcShxxYVU49T0B2+W6ty2lsL158z4zzJDkPd6P93l/fgnDEUlCIQW/fHQAiv2hZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZErE7jKTW5xVj3Dame4xnG8skyG6F6c4OjrBRfcByRLIHq5wdtzARecO0/wgzW7PYor7zjlOjo5wenmDx2wVc6txjGari2G8+PkxcScKhqLOWq1CmAHnu33I66QBjYpGMSrXhzbt3uxQre1ARK8O/taekCTLyKclBofrj1fYTebMp2H4DH2dqDkc7zemDVIGRoWaGLH44I76EZFXJ35rc1KM2bZcjoqPi2UHmQVDodMZlSxHDjUY7KWvVCnH9CyTpvn9y/InL1ZNA4MVTafxwR31QyKPddSoGxa9SfmhobxdZuTTMHtMSTLt0cC3ZfAgpD0aqNDs/9/m5FdWS60mQn6sSvLXt+2wCwyvh2heT3EKAKdNNGtfMI0z4KKx/w0dQPb4Gbn2NxaPUyw/XaJ6kFaA5K6FzjDH8vmFahOd+1tcnvx3vWX8iOlCw5fPj4hxibMDxbcVb1IfedRrOi3bpm3btG2LugZW7MHLo3LHZbaMfFrOgFFgsqIJhm8a+nOGrqA3OuCMLsZsW22OJj511Oi+vGNsVuTIE2yH6V7DeYPMOQe2zvazfWHi1oi6x2h9nPY9BukeIttMKOYBzYpG8Rab5ZzpQffYOUPHoheVJGf0dbDmjresW3B+gNi2lllO2jTEgM9jKPomUbHYL0jOQ4q6QTcYrfbUnVl31NPAmTMwK9TscN1+yr5dpyFcCkunIXz2PIembrOfkmTEoO2z5wmK3ozlbEBRN+hHKUPXYRC9vyef3ybN/DpRc9eZfclZz2TddOgKk7rp0vfbFIZOJyxYpgMK3eX4aznLZdsxqdv9d/XbVjKLSUCh16g7A842J8d8zMDRWYFGwx1wVo7omD7fcydYRgN6QqdWt+mPUpJkOurRMTSiolP4q4ESeSbdMckypLBW97ppz6IISbJgUZAsB7StPguSRShoCo+99y67xYR9z2Zd0+kEE85JlrOQvqiv+sEJOJ6THLs0/RnJOfuWzUFJMvJotSckZ/TNtfixS2tdLrDeupX8m60SoOOzKwzjq+8vnJzj6j7G1f36OL9DftJYJUc7Um22cDNs4Wbj3OlFB/cXHdxvnIufKlRRxRLL1dsVywyPwylQjZGvU5Ljyw6a3S7Q+EEmsy3HZ/h084BPmwE2LtEdXqI73DiXPAWIp6ytegx8n2I9lau+cHUb9vtsNomBZuNgGee2LIbXGFZbaLXOnwZW/hDj/OYMD90HfMCDtp/CfmUen2L5eIv7gz+XzJFkObI4QRbHyPIYcZYhjnPkSYJqo4HF3RW6d5+R51P88fstrqcNnF+0cJZ00Rlm75oBr7NElmTI4xhZFiPOckzjDEkcI08S/PXnFHGeYBrn63LJ6jvkGbJk98iOSPVfE1lQP4FJhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkT8A7YI56A7YlkCAAAAAElFTkSuQmCC"/>
     * @param resultClass number type class.
     * @return amplitude
     */
    public final <N extends Number> IterableResultMap<K, N> mean(Class<N> resultClass) {
        return getIteratorAsGroup().mean(resultClass);
    }

    /**
     * <p>
     * A mean is a mathematical term, that describes the average of a sample.<br/>
     * In Statistics, the definition of the mean is similar to the average.<br/>
     * But, it can also be defined as the sum of the smallest value and the largest value in the given data set divided by 2.
     * </p>
     * <p>
     * In Statistics, the total amplitude At of a set of values
     * is the difference between the highest and lowest value of the sample, as shown below:
     * </p>
     * <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHMAAAAwCAYAAAAij0UkAAAACXBIWXMAAA7EAAAOxAGVKw4bAAAE1ElEQVR4nO2bL3DiWBzHv725GXCpo+7qwAWXutSlLnUPl3WpAxdkXeqoSx114FJzQ8XNsA5ccoqcShxxYVU49T0B2+W6ty2lsL158z4zzJDkPd6P93l/fgnDEUlCIQW/fHQAiv2hZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZErE7jKTW5xVj3Dame4xnG8skyG6F6c4OjrBRfcByRLIHq5wdtzARecO0/wgzW7PYor7zjlOjo5wenmDx2wVc6txjGari2G8+PkxcScKhqLOWq1CmAHnu33I66QBjYpGMSrXhzbt3uxQre1ARK8O/taekCTLyKclBofrj1fYTebMp2H4DH2dqDkc7zemDVIGRoWaGLH44I76EZFXJ35rc1KM2bZcjoqPi2UHmQVDodMZlSxHDjUY7KWvVCnH9CyTpvn9y/InL1ZNA4MVTafxwR31QyKPddSoGxa9SfmhobxdZuTTMHtMSTLt0cC3ZfAgpD0aqNDs/9/m5FdWS60mQn6sSvLXt+2wCwyvh2heT3EKAKdNNGtfMI0z4KKx/w0dQPb4Gbn2NxaPUyw/XaJ6kFaA5K6FzjDH8vmFahOd+1tcnvx3vWX8iOlCw5fPj4hxibMDxbcVb1IfedRrOi3bpm3btG2LugZW7MHLo3LHZbaMfFrOgFFgsqIJhm8a+nOGrqA3OuCMLsZsW22OJj511Oi+vGNsVuTIE2yH6V7DeYPMOQe2zvazfWHi1oi6x2h9nPY9BukeIttMKOYBzYpG8Rab5ZzpQffYOUPHoheVJGf0dbDmjresW3B+gNi2lllO2jTEgM9jKPomUbHYL0jOQ4q6QTcYrfbUnVl31NPAmTMwK9TscN1+yr5dpyFcCkunIXz2PIembrOfkmTEoO2z5wmK3ozlbEBRN+hHKUPXYRC9vyef3ybN/DpRc9eZfclZz2TddOgKk7rp0vfbFIZOJyxYpgMK3eX4aznLZdsxqdv9d/XbVjKLSUCh16g7A842J8d8zMDRWYFGwx1wVo7omD7fcydYRgN6QqdWt+mPUpJkOurRMTSiolP4q4ESeSbdMckypLBW97ppz6IISbJgUZAsB7StPguSRShoCo+99y67xYR9z2Zd0+kEE85JlrOQvqiv+sEJOJ6THLs0/RnJOfuWzUFJMvJotSckZ/TNtfixS2tdLrDeupX8m60SoOOzKwzjq+8vnJzj6j7G1f36OL9DftJYJUc7Um22cDNs4Wbj3OlFB/cXHdxvnIufKlRRxRLL1dsVywyPwylQjZGvU5Ljyw6a3S7Q+EEmsy3HZ/h084BPmwE2LtEdXqI73DiXPAWIp6ytegx8n2I9lau+cHUb9vtsNomBZuNgGee2LIbXGFZbaLXOnwZW/hDj/OYMD90HfMCDtp/CfmUen2L5eIv7gz+XzJFkObI4QRbHyPIYcZYhjnPkSYJqo4HF3RW6d5+R51P88fstrqcNnF+0cJZ00Rlm75oBr7NElmTI4xhZFiPOckzjDEkcI08S/PXnFHGeYBrn63LJ6jvkGbJk98iOSPVfE1lQP4FJhJIpEUqmRCiZEqFkSoSSKRFKpkQomRKhZEqEkikRSqZEKJkSoWRKhJIpEUqmRCiZEqFkSoSSKRFKpkT8A7YI56A7YlkCAAAAAElFTkSuQmCC"/>
     * @param mountFun function to get target number
     * @param <N>      result number type
     * @return result number
     */
    public final <N extends Number> IterableResultMap<K, N> mean(CollectionHelper.FunctionMount<V, N> mountFun) {
        return getIteratorAsGroup().mean(mountFun);
    }

    /**
     * Recover minimum value of collection
     *
     * @return minimum value
     */
    public final IterableResultMap<K, V> min() {
        return getIteratorAsGroup().min();
    }

    /**
     * Recover minimum value of collection
     *
     * @param mountFun function to get Comparable element target
     * @param <C>      result comparable element
     * @return minimum value
     */
    public final <C extends Comparable<C>> IterableResultMap<K, V> min(CollectionHelper.FunctionMount<V, C> mountFun) {
        return getIteratorAsGroup().min(mountFun);
    }

    /**
     * Recover maximum value of collection
     *
     * @return maximum value
     */
    public final IterableResultMap<K, V> max() {
        return getIteratorAsGroup().max();
    }

    /**
     * Recover maximum value of collection
     *
     * @param mountFun function to get Comparable element target
     * @param <C>      result comparable element
     * @return maximum value
     */
    public final <C extends Comparable<C>> IterableResultMap<K, V> max(CollectionHelper.FunctionMount<V, C> mountFun) {
        return getIteratorAsGroup().max(mountFun);
    }

    /**
     * Filter current group to get only dessired sample (key-values).
     * @param checkFun function to filter and recover only dessired sample.
     * @return new group with filtered values.
     */
    public final IterableResultGroup<K, V> sample(CollectionHelper.CompareEntryValid<K> checkFun) {
        return getIteratorAsGroup().sample(checkFun);
    }

    /**
     * Filter current group to get only dessired sample (key-values).
     * @param keys key names to filter and recover only dessired sample.
     * @return new group with filtered values.
     */
    @SafeVarargs
    public final IterableResultGroup<K, V> sample(K... keys) {
        if(keys.length == 0){
            throw new IllegalArgumentException("Key name not set!");
        }
        return getIteratorAsGroup().sample(k0 -> CollectionHelper.any(keys, k1 -> equalsKeys(k0, k1)));
    }

    /**
     * Filter current group setting max amount.
     * @param count max amount.
     * @return a new iterable group with max amount.
     */
    public final IterableResultGroup<K, V> amount(int count) {
        if(count < 1) {
            throw new IllegalArgumentException("Count can not be less then 1!");
        }
        return getIteratorAsGroup().amount(count);
    }

    private boolean equalsKeys(K k0, K k1) {
        return k0 == k1 || (
                k0 instanceof String &&
                        k1 instanceof String ?
                        equalsIgnoreCase((String) k0, (String) k1) :
                        k0.equals(k1));
    }

    private boolean equalsIgnoreCase(String str, String anotherString) {
        //noinspection StringEquality
        return str == anotherString ||
                (str != null &&
                        anotherString != null &&
                        str.length() == anotherString.length() &&
                        str.regionMatches(true, 0, anotherString, 0, str.length())
                );
    }
    //endregion
}
