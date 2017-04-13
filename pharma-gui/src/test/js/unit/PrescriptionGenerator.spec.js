"use strict";

describe("List select", function() {
    it("filters data", function() {
        var evenNumbers = function(string) {
            return function(o) { return o % 2 == 0; }
        };

        var selected = "3";
        var list = new ListSelect({
            propsData: {
                value: selected,
                objectFilter: evenNumbers,
                allOptions: [1,2,3,22,33,44,55]
            }
        }).$mount();

        expect(list.options).to.contain(22).not.contain(33);
    });

    it("filters patients", function() {
        var evenNumbers = function(string) {
            return function(o) { return o % 2 == 0; }
        };

        var list = new ListSelect({
            propsData: {
                objectFilter: filterPatients,
                allOptions: [
                    {firstName: "Jack", lastName: "Jackson"},
                    {firstName: "Jill", lastName: "Jillson"}
                ]
            }
        }).$mount();
        list.itemFilter = "Jill";

        expect(list.options).
            to.contain({firstName: "Jill", lastName: "Jillson"}).
            not.contain({firstName: "Jack", lastName: "Jackson"});
    });

});


describe("Object filter", function() {
    function itMatches(medication, string, filterFunction) {
        return it("matches '" + string + "'", function() {
            expect(filterFunction(string)(medication)).to.be.true;
        });
    }
    function itExcludes(medication, string, filterFunction) {
        return it("excludes '" + string + "'", function() {
            expect(filterFunction(string)(medication)).to.be.false;
        });
    }

    describe("for patients", function() {
        var jackJackson = {
            firstName: "Jack", lastName: "Jackson", nationalId: "25046815397"
        };

        itMatches(jackJackson, "jacks", filterPatients);
        itMatches(jackJackson, "jack jacks", filterPatients);
        itMatches(jackJackson, "jackson, jac", filterPatients);
        itExcludes(jackJackson, "jills", filterPatients);
        itExcludes(jackJackson, "jack smith", filterPatients);

        itMatches(jackJackson, "25046815", filterPatients);
        itExcludes(jackJackson, "26046815", filterPatients);
    });

    describe("for practitioners", function() {
        var practitioner = {"name":"ANDERS BENGT PETER EJERHED","id":"8698074"};

        itMatches(practitioner, "Anders", filterPractitioners);
        itMatches(practitioner, "EJERHED", filterPractitioners);
        itExcludes(practitioner, "terje", filterPractitioners);
    });
    describe("for medication", function() {
        var ritalin = {
            "atc":"N06BA04","productId":"500595","display":"Ritalin Tab 10 mg"
        };

        itMatches(ritalin, "rITal", filterMedications);
        itExcludes(ritalin, "lin", filterMedications);
        itExcludes(ritalin, "something", filterMedications);

        itMatches(ritalin, "n06", filterMedications);
        itExcludes(ritalin, "n07", filterMedications);

        itMatches(ritalin, "500595", filterMedications);
        itMatches(ritalin, "500", filterMedications);
        itExcludes(ritalin, "500596", filterMedications);
    });
});
