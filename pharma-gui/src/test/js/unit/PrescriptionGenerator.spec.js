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


describe("Data filters", function() {
    describe("Filter patients");
    describe("Filter practitioners");
    describe("Filter medications");
});
