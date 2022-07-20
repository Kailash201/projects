from bs4 import BeautifulSoup as soup
import pandas as pd
import requests

requests.packages.urllib3.util.ssl_.DEFAULT_CIPHERS = "TLS13-CHACHA20-POLY1305-SHA256:TLS13-AES-128-GCM-SHA256:TLS13-AES-256-GCM-SHA384:ECDHE:!COMPLEMENTOFDEFAULT"
import sys

################## TO READ LIST OF COMPANY ###########################
# stocklistex = pd.read_csv('/storage/emulated/0/Download/abc.csv')
# sl = stocklistex.values.tolist()
stocklist = []
# for ss in sl:
#    stocklist.append(ss[0].lower())

totaldf = pd.DataFrame(columns=[('name'), ('numbers')])
stlist = []
tt = []
counts = 0

# sys.stdout = open('stocklist.txt','w')

headers = {
    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36'}

stock = input("Stock Symbol: ")
for c in range(1):  ###################### CHANGE TO LIST SIZE IF USING LIST ####################
    # print("\n" + str(stocklist[c + 45]) + " " + str(c + 45))

    stocklist.append(str(stock))

    page1 = requests.get(('https://stockanalysis.com/stocks/' + str(stock) + '/financials/balance-sheet/'),
                         headers=headers).text
    page = requests.get(('https://stockanalysis.com/stocks/' + str(stock) + '/financials/cash-flow-statement/'),
                        headers=headers).text
    page2 = requests.get(('https://stockanalysis.com/stocks/' + str(stock) + '/financials/'), headers=headers).text
    page4 = requests.get(('https://stockanalysis.com/stocks/' + str(stock) + '/'), headers=headers).text
    page5 = requests.get(
        ('https://sg.finance.yahoo.com/quote/' + str(stock).upper() + '/financials?p=' + str(stock).upper()),
        headers=headers).text
    page6 = requests.get(('https://sg.finance.yahoo.com/quote/' + str(stock).upper() + '?p=' + str(stock).upper()),
                         headers=headers).text
    page7 = requests.get(
        ('https://sg.finance.yahoo.com/quote/' + str(stock).upper() + '/key-statistics?p=' + str(stock).upper()),
        headers=headers).text

    pagesoup = soup(page, "html.parser")
    pagesoup1 = soup(page1, "html.parser")
    pagesoup2 = soup(page2, "html.parser")
    pagesoup4 = soup(page4, "html.parser")
    pagesoup5 = soup(page5, "html.parser")
    pagesoup6 = soup(page6, "html.parser")
    pagesoup7 = soup(page7, "html.parser")


    def scrapper(source):
        statslist = []  # Create empty list
        years = []
        sa_multi = 0
        ls1 = []
        lsa1 = []

        for q in source.find_all("td"):
            # Find all data structure that is ‘td’ (values)
            if q.find("span") is not None and q.find("span").string != q.string:
                statslist.append(q.find("span").string)
                continue

            statslist.append(q.string)  # add each element one by one to the list
            ls1 = list(filter(None, statslist))

        # print(ls1)

        for a in source.find_all("th"):
            # Find all data structure that is ‘th’ (years)
            years.append(a.string)  # add each element one by one to the list1
            lsa1 = list(filter(None, years))
        dd = []
        dd1 = []
        for k in source.find_all("div", class_="info-long svelte-f7kao3"):
            # check if in millions
            if 'Financials in millions USD' in k.string:
                sa_multi = 1000000

        # print(dd)

        # make 2d array
        stats = []
        templist = 0
        counter = 0
        stats.append([])

        for x in ls1:
            stats[templist].append(x.string)
            counter = counter + 1
            if counter % (len(lsa1) - 1) == 0:
                # and x != ls1[-1]:
                stats.append([])
                templist = templist + 1

        pstats = pd.DataFrame(stats, columns=lsa1[:len(lsa1) - 1])
        pstats.set_index("Year", inplace=True)

        return pstats, sa_multi


    def market_cap(link):
        statslist = []
        years = []
        sa_multi = 0
        pfx = ['M', 'B', 'T']
        for q in link.find_all("td"):
            statslist.append(q.string)
        ls1 = list(filter(None, statslist))
        mc = ls1[1]
        prefix = mc[len(mc) - 1]
        if prefix == pfx[2]:
            return float(mc.replace(prefix, '')) * 1000000000000
        if prefix == pfx[1]:
            return float(mc.replace(prefix, '')) * 1000000000
        if prefix == pfx[0]:
            return float(mc.replace(prefix, '')) * 1000000


    def market_shares(link):
        statslist = []
        years = []
        sa_multi = 0
        pfx = ['M', 'B', 'T']
        for q in link.find_all("td"):
            statslist.append(q.string)
        ls1 = list(filter(None, statslist))
        # print(ls1)
        ms = ls1[ls1.index('Net Income (ttm)') + 2]

        prefix = ms[len(ms) - 1]
        if prefix == pfx[2]:
            return float(ms.replace(prefix, '')) * 1000000000000
        if prefix == pfx[1]:
            return float(ms.replace(prefix, '')) * 1000000000
        if prefix == pfx[0]:
            return float(ms.replace(prefix, '')) * 1000000


    def beta(link):
        templist = []
        holder = []
        # print(link.prettify())
        for a in link.find_all("td"):
            templist.append(a.string)
        holder = list(filter(None, templist))
        # print(holder)
        for b in holder:
            if b == 'Beta (5Y monthly)':
                beta = holder[holder.index(b) + 1]
                # print(beta)
                return float(beta)


    def price(link):
        templist = []
        holder = []
        # print(link.prettify())
        for a in link.find_all("td"):
            templist.append(a.string)

        holder = list(filter(None, templist))
        # print(holder)
        for b in holder:
            if b == 'Previous close':
                price = holder[holder.index(b) + 1]
                # print(price)
                return float(price)


    def getintexp(source):

        tlist = []  # Create empty list
        years1 = []
        for q in source.find_all("span"):
            tlist.append(q.string)
            years1 = list(filter(None, tlist))
        ab = pd.DataFrame(years1)
        l_ie = ab.values.tolist()

        word = [['Interest expense'], ['All numbers in thousands'], ['All numbers in millions']]
        cutter = 0
        for x in range(len(ab)):
            if l_ie[x] == word[1]:
                yah_multi = 1000
            #    print(yah_multi)
            if l_ie[x] == word[2]:
                yah_multi = 1000000
            if l_ie[x] == word[0]:
                cutter = x
                intexp1 = str(l_ie[x + 2]).replace(',', '')
                lenexp = len(intexp1)
                # print(intexp1)
                intexp = intexp1[2:lenexp - 2]

                return float(intexp) * yah_multi


    def cfgrowthrate(dataf):
        fcf = dataf.loc['Free Cash Flow']
        fcf3 = fcf[:11]
        fcf1 = fcf3.values.tolist()
        rfcf = fcf1[::-1]
        fcf_gr = []
        gr = []

        # remove commas
        rfcf1 = []
        iap = []
        for aa in rfcf:
            if str(aa) == "-":
                ff = str(aa).replace('-', '0')
                iap.append(ff)
            if str(aa) != "-":
                iap.append(aa)

        for i in iap:
            ff = str(i).replace(',', '')
            ii = float(ff)
            rfcf1.append(ii)

        # print(rfcf1)
        # calculate growth rate
        for n in range(len(rfcf1) - 1):
            if rfcf1[n] == 0:
                n = n + 1
            if rfcf1[n] * -1 <= 0:
                growth = (rfcf1[n + 1] - rfcf1[n]) / rfcf1[n]
                gr.append(growth * 100)
            if rfcf1[n] * -1 >= 0:
                growth = (rfcf1[n + 1] - rfcf1[n]) / rfcf1[n]
                gr.append(growth * 100 * -1)
        fcf_gr = gr[::-1]
        # print(dataf.loc['Free Cash Flow'])
        # print(fcf_gr)
        # stats on fcf growth rate

        p_fcfgr = pd.Series(fcf_gr)
        fcf_mean = p_fcfgr.mean()
        # print(fcf_mean)
        fcf_sd = p_fcfgr.std()
        fcf_sem = p_fcfgr.sem()

        # CI of 80%
        tvalue = 1.383
        lowmean80 = fcf_mean - fcf_sem * tvalue
        highmean80 = fcf_mean + fcf_sem * tvalue
        ci80 = str(lowmean80) + ' to ' + str(highmean80)

        # CI of 90%
        tvalue = 1.833
        lowmean90 = fcf_mean - fcf_sem * tvalue
        highmean90 = fcf_mean + fcf_sem * tvalue
        ci90 = str(lowmean90) + ' to ' + str(highmean90)

        # CI of 95%
        tvalue = 2.262
        lowmean95 = fcf_mean - fcf_sem * tvalue
        highmean95 = fcf_mean + fcf_sem * tvalue
        ci95 = str(lowmean95) + ' to ' + str(highmean95)

        # CI of 98%
        tvalue = 2.821
        lowmean98 = fcf_mean - fcf_sem * tvalue
        highmean98 = fcf_mean + fcf_sem * tvalue
        ci98 = str(lowmean98) + ' to ' + str(highmean98)

        fg = pd.DataFrame([fcf_mean, fcf_sem, ci80, ci90, ci95, ci98],
                          index=["Free cash flow growth rate: mean", "Free cash flow growth rate: sem", "Free cash "
                                                                                                        "flow growth "
                                                                                                        "rate: CI of "
                                                                                                        "80%: ",
                                 "Free cash flow growth rate: CI of 90%: ", "Free cash flow growth rate: CI of 95%: "
                                                                            "", "Free cash flow growth rate: CI of "
                                                                                "98%: "])
        return fg, lowmean90, lowmean95, lowmean98, str(fcf1[0]).replace(',', ''), lowmean80, fcf_mean


    def comma_remover(df):
        pt = []
        ip = []
        sym = pd.DataFrame(['-'])
        for aa in df:
            if str(aa) == "-":
                ff = str(aa).replace('-', '0')
                ip.append(ff)
            if str(aa) != "-":
                ip.append(aa)

        for i in ip:
            ff = str(i).replace(',', '')
            ii = float(ff)
            pt.append(ii)
        return pt


    #######################DISCOUNTED CASH FLOW CALCULATIONS#######################
    #####WACC CALCULATION

    bb_bs = scrapper(pagesoup1)[0]
    bb_is = scrapper(pagesoup2)[0]

    sam = scrapper(pagesoup)[1]

    try:
        pretax = bb_is.loc['Pretax Income']
        income_tax = bb_is.loc['Income Tax']
        f_pretax = comma_remover(pretax)
        f_income_tax = comma_remover(income_tax)

        income_taxrate = (f_income_tax[0] / f_pretax[0])



    except KeyError:
        print("MISSING PRETAX IMCOME OR INCOME TAX")

    try:
        cd = bb_bs.loc['Current Debt']
        otc = bb_bs.loc['Other Current Liabilities']
        ltd = bb_bs.loc['Long-Term Debt']
        oltd = bb_bs.loc['Other Long-Term Liabilities']
    except KeyError:
        print("MISSING ONE OF THE LIABILITIES")
        continue

    _cd = comma_remover(cd)
    _otc = comma_remover(otc)
    _ltd = comma_remover(ltd)
    _oltd = comma_remover(oltd)

    if sam == 0:
        print("Multiple is zero")

    total_debt = (_cd[0] + _otc[0] + _ltd[0] + _oltd[0]) * sam

    try:
        intexp = getintexp(pagesoup5)
        intexprate = (intexp / total_debt)

    except TypeError:
        print("NO INTEREST EXPENSE")
        continue

    try:
        mc = market_cap(pagesoup4)

    except:
        print("MISSING MARKET CAP")
        continue

    weighted_debt = (total_debt / (total_debt + mc))
    weighted_equity = 1 - weighted_debt
    try:
        beta = beta(pagesoup6)
    except:
        print("MISSING BETA")
        continue
    try:
        price = price(pagesoup6)
    except:
        print("MISSING PRICE")
        continue

    capm = (1.6 + beta * (7 - 1.6)) / 100

    wacc = weighted_debt * intexprate * (1 - income_taxrate) + weighted_equity * capm

    # print(wacc * 100)
    # print(capm)

    ##################Cashflowprojection

    fcf = scrapper(pagesoup)[0]
    fcfmulti = scrapper(pagesoup)[1]
    data = cfgrowthrate(fcf)
    gr = data[0]
    lm80 = float(data[5])
    lm90 = data[1]
    lm95 = data[2]
    lm98 = data[3]
    latestcf = float(data[4])
    fcfmean = float(data[6])

    try:
        sharesout = market_shares(pagesoup4)
    except:
        print("MISSING SHARES OUT")
        continue

    ##CI80
    newcf = latestcf
    newcf1 = latestcf
    futurecf = []
    meanfuturecf = []
    rate = (lm80 / 100) + 1
    meanrate = (fcfmean / 100) + 1
    r5 = 1.05

    for a in range(4):
        newcf = newcf * (rate)
        newcf1 = newcf1 * meanrate

        futurecf.append(newcf * fcfmulti)
        meanfuturecf.append(newcf1 * fcfmulti)

    terminalv = (futurecf[len(futurecf) - 1] * (1 + 2.5 / 100)) / (wacc - 2.5 / 100)
    meanterminalv = (meanfuturecf[len(meanfuturecf) - 1] * (1 + 2.5 / 100)) / (wacc - 2.5 / 100)

    discf = [latestcf * fcfmulti]
    discf1 = [latestcf * fcfmulti]  # for mean
    fyear = 1
    for cash in range(4):
        dcash = (futurecf[cash] / ((1 + (wacc)) * fyear))
        meandcash = (meanfuturecf[cash] / ((1 + (wacc)) * fyear))

        discf.append(dcash)
        discf1.append(meandcash)

        fyear = fyear + 1

    totaldcf = sum(discf) + terminalv
    intrinv = totaldcf / sharesout

    meantotaldcf = sum(discf1) + meanterminalv
    meanintrinv = meantotaldcf / sharesout

    df2 = pd.DataFrame([intrinv, meanintrinv, str(stocklist[c]) + " " + str(c), wacc * 100, capm * 100, price],
                       index=['Intrinsic value at CI80: ', 'Intrinsic value at mean', 'Stock Symbol', 'WACC', 'CAPM',
                              'Stock Price'])
    df3 = gr.append(df2)
    print(df3)

# sys.stdout.close()
# print(c)
