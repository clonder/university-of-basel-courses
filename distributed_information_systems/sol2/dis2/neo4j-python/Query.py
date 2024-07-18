from neo4j import GraphDatabase
import numpy as np
import pandas as pd

import matplotlib.pyplot as plt


def main():
    with GraphDatabase.driver("bolt://localhost:7687", auth=("neo4j", r"Tp6Ruboz%5up")) as driver:
        with driver.session(database="neo4j") as session:
            df = query_flights_by_years(session)
            plot_bar(df, title="Flights by Year", ylabel="Number of Flights", xlabel="Year", logaritmic=True)

            df = query_flights_by_years(session)
            plot_bar(df, title="Planes by Year", ylabel="Number of Planes", xlabel="Year", logaritmic=True)


def query_flights_by_years(session):
    query = """// q2.a.1
            MATCH (F:Flight)
            RETURN labels(F), F.year, count(*)
            """
    result = session.run(query).data()
    df = pd.DataFrame(result)
    df = df.rename(columns={"F.year": "axis", "count(*)": "value1"})
    df = df.sort_values(by='axis')
    return df


def query_planes_by_years(session):
    query = """
                // q2.a.2
                MATCH (F:Flight)-[r:executedWith]-(P:Plane)
                RETURN F.year, COUNT(DISTINCT P.tailnum)
            """
    result = session.run(query).data()
    df = pd.DataFrame(result)
    df = df.rename(columns={"F.year": "axis", "COUNT(DISTINCT P.tailnum)": "value1"})
    df = df.sort_values(by='axis')
    return df


def plot_bar(df, n=1, title="No Title", ylabel="No Y Label", xlabel="No X Label", legend=None, logaritmic=False):
    if legend is None:
        legend = []
    fig, ax = plt.subplots(layout='constrained')

    if logaritmic:
        ax.set_yscale('log')

    width = 0.36  # the width of the bars
    mid = 0
    d = width / n
    x = np.arange(len(df))

    hatches = ['//', '...', 'x', '\\', '*', 'o', 'O', '.']

    for ic in range(0, n):
        plt.bar(x - mid + d * ic, df[f'value{ic + 1}'], width, fill=False, hatch=hatches[ic])

    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    plt.title(title)
    if legend:
        leg = plt.legend(legend, loc='best')
        leg.get_frame().set_linewidth(0.0)
        leg.get_frame().set_facecolor('none')

    plt.xticks(x, df['axis'], rotation='vertical')
    plt.savefig(f"./{title}.pdf")
    plt.show()


if __name__ == "__main__":
    main()
